package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.VisitHistory;
import popfri.spring.domain.enums.Gender;
import popfri.spring.repository.RecHistoryRepository;
import popfri.spring.repository.VisitHistoryRepository;
import popfri.spring.web.dto.HistoryResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseEmitters {
    private final VisitHistoryRepository visitHistoryRepository;
    private final RecHistoryRepository recHistoryRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    SseEmitter add(SseEmitter emitter) {
        this.emitters.add(emitter);
        log.info("new emitter added: {}", emitter);
        log.info("emitter list size: {}", emitters.size());
        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            this.emitters.remove(emitter);    // 만료되면 리스트에서 삭제
        });
        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitter.complete();
        });

        return emitter;
    }

    public void remove(SseEmitter emitter) {
        this.emitters.remove(emitter);
    }

    public <T> List<HistoryResponse.VisitAnalysisDTO> getAnalysisData(
            String date, String type,
            BiFunction<LocalDateTime, LocalDateTime, List<T>> baseQuery,
            Function<T, Integer> tmdbIdExtractor,
            Function<T, String> movieNameExtractor
    ) {
        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;
        LocalDate birthStart;
        LocalDate birthEnd;
        int currentYear = today.getYear();

        switch (date.toLowerCase()) {
            case "day":
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            case "week":
                start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                break;
            case "month":
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + date);
        }

        List<T> data;

        switch (type) {
            case "default":
                data = baseQuery.apply(start, end);
                break;
            case "male":
                data = baseQuery.apply(start, end); // override 아래에서
                break;
            case "female":
                data = baseQuery.apply(start, end); // override 아래에서
                break;
            case "10":
                birthStart = LocalDate.of(currentYear - 19, 1, 1);
                birthEnd = LocalDate.of(currentYear - 10, 12, 31);
                data = baseQuery.apply(start, end); // override 아래에서
                break;
            case "20":
                birthStart = LocalDate.of(currentYear - 29, 1, 1);
                birthEnd = LocalDate.of(currentYear - 20, 12, 31);
                data = baseQuery.apply(start, end);
                break;
            case "30":
                birthStart = LocalDate.of(currentYear - 39, 1, 1);
                birthEnd = LocalDate.of(currentYear - 30, 12, 31);
                data = baseQuery.apply(start, end);
                break;
            case "40":
                birthStart = LocalDate.of(currentYear - 200, 1, 1);
                birthEnd = LocalDate.of(currentYear - 40, 12, 31);
                data = baseQuery.apply(start, end);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 유형: " + type);
        }

        return data.stream()
                .collect(Collectors.groupingBy(tmdbIdExtractor))
                .entrySet().stream()
                .map(entry -> {
                    T sample = entry.getValue().get(0);
                    return new HistoryResponse.VisitAnalysisDTO(
                            entry.getKey(),
                            movieNameExtractor.apply(sample),
                            entry.getValue().size()
                    );
                })
                .sorted(Comparator.comparingLong(HistoryResponse.VisitAnalysisDTO::getCount).reversed())
                .collect(Collectors.toList());
    }

    public List<HistoryResponse.VisitAnalysisDTO> getVisitAnalysisData(String date, String type) {
        return getAnalysisData(
                date,
                type,
                (start, end) -> switch (type) {
                    case "default" -> visitHistoryRepository.findAllByUpdatedAtBetween(start, end);
                    case "male" -> visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.MALE);
                    case "female" -> visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.FEMALE);
                    case "10" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 19, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 10, 12, 31);
                        yield visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "20" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 29, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 20, 12, 31);
                        yield visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "30" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 39, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 30, 12, 31);
                        yield visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "40" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 200, 1, 1); // 200살까지 고려
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 40, 12, 31);
                        yield visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    default -> throw new IllegalArgumentException("유형 오류");
                },
                VisitHistory::getTmdbId,
                VisitHistory::getMovieName
        );
    }

    public List<HistoryResponse.VisitAnalysisDTO> getRecommendAnalysisData(String date, String type) {
        return getAnalysisData(
                date,
                type,
                (start, end) -> switch (type) {
                    case "default" -> recHistoryRepository.findAllByUpdatedAtBetween(start, end);
                    case "male" -> recHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.MALE);
                    case "female" -> recHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.FEMALE);
                    case "10" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 19, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 10, 12, 31);
                        yield recHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "20" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 29, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 20, 12, 31);
                        yield recHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "30" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 39, 1, 1);
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 30, 12, 31);
                        yield recHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    case "40" -> {
                        LocalDate bs = LocalDate.of(LocalDate.now().getYear() - 200, 1, 1); // 200살까지 고려
                        LocalDate be = LocalDate.of(LocalDate.now().getYear() - 40, 12, 31);
                        yield recHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, bs, be);
                    }
                    default -> throw new IllegalArgumentException("유형 오류");
                },
                RecHistory::getTmdbId,
                RecHistory::getMovieName
        );
    }

    public void sendDailyVisitAnalysis(String type) {
        List<HistoryResponse.VisitAnalysisDTO> data = getVisitAnalysisData("day", type);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("visit-analysis-" + type)
                        .data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public void sendDailyVisitAnalysisToAllTypes() {
        List<String> types = List.of("default", "male", "female", "10", "20", "30", "40");

        for (String type : types) {
            try {
                sendDailyVisitAnalysis(type);
            } catch (Exception e) {
                System.err.println("SSE 전송 실패 (type=" + type + "): " + e.getMessage());
            }
        }
    }

    public void sendDailyRecommendAnalysis(String type) {
        List<HistoryResponse.VisitAnalysisDTO> data = getRecommendAnalysisData("day", type);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("recommend-analysis-" + type)
                        .data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public void sendDailyRecommendAnalysisToAllTypes() {
        List<String> types = List.of("default", "male", "female", "10", "20", "30", "40");

        for (String type : types) {
            try {
                sendDailyRecommendAnalysis(type);
            } catch (Exception e) {
                System.err.println("SSE 전송 실패 (type=" + type + "): " + e.getMessage());
            }
        }
    }
}
