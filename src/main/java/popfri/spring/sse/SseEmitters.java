package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.VisitHistory;
import popfri.spring.domain.enums.Gender;
import popfri.spring.repository.RecHistoryRepository;
import popfri.spring.repository.VisitHistoryRepository;
import popfri.spring.web.dto.HistoryResponse;

import java.time.*;
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

    public SseEmitter add(SseEmitter emitter) {
        emitters.add(emitter);
        log.info("new emitter added: {}", emitter);
        log.info("emitter list size: {}", emitters.size());
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public Gender parseGender(String genderStr) {
        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate[] getBirthRange(String ageGroup) {
        int year = LocalDate.now().getYear();
        return switch (ageGroup) {
            case "10" -> new LocalDate[]{LocalDate.of(year - 19, 1, 1), LocalDate.of(year - 10, 12, 31)};
            case "20" -> new LocalDate[]{LocalDate.of(year - 29, 1, 1), LocalDate.of(year - 20, 12, 31)};
            case "30" -> new LocalDate[]{LocalDate.of(year - 39, 1, 1), LocalDate.of(year - 30, 12, 31)};
            case "40" -> new LocalDate[]{LocalDate.of(year - 200, 1, 1), LocalDate.of(year - 40, 12, 31)};
            default -> throw new IllegalArgumentException("Invalid age group: " + ageGroup);
        };
    }

    private LocalDateTime[] getDateRange(String date) {
        LocalDate today = LocalDate.now();
        return switch (date.toLowerCase()) {
            case "day" -> new LocalDateTime[]{today.atStartOfDay(), today.atTime(LocalTime.MAX)};
            case "week" -> new LocalDateTime[]{
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(),
                    today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX)};
            case "month" -> new LocalDateTime[]{
                    today.withDayOfMonth(1).atStartOfDay(),
                    today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX)};
            default -> throw new IllegalArgumentException("Invalid date format: " + date);
        };
    }

    public <T> List<HistoryResponse.VisitAnalysisDTO> getAnalysisData(
            String date, String type,
            BiFunction<LocalDateTime, LocalDateTime, List<T>> baseQuery,
            Function<T, Integer> tmdbIdExtractor,
            Function<T, String> movieNameExtractor
    ) {
        LocalDateTime[] dateRange = getDateRange(date);
        List<T> data = baseQuery.apply(dateRange[0], dateRange[1]);

        return data.stream()
                .collect(Collectors.groupingBy(tmdbIdExtractor))
                .entrySet().stream()
                .map(entry -> new HistoryResponse.VisitAnalysisDTO(
                        entry.getKey(),
                        movieNameExtractor.apply(entry.getValue().get(0)),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparingLong(HistoryResponse.VisitAnalysisDTO::getCount).reversed())
                .collect(Collectors.toList());
    }

    public List<HistoryResponse.VisitAnalysisDTO> getVisitAnalysisData(String date, String type) {
        return getAnalysisData(date, type, (start, end) -> switch (type) {
            case "default" -> visitHistoryRepository.findAllByUpdatedAtBetween(start, end);
            case "male" -> visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.MALE);
            case "female" -> visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.FEMALE);
            case "10", "20", "30", "40" -> {
                LocalDate[] range = getBirthRange(type);
                yield visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, range[0], range[1]);
            }
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }, VisitHistory::getTmdbId, VisitHistory::getMovieName);
    }

    public List<HistoryResponse.VisitAnalysisDTO> getRecommendAnalysisData(String date, String type) {
        return getAnalysisData(date, type, (start, end) -> switch (type) {
            case "default" -> recHistoryRepository.findAllByUpdatedAtBetween(start, end);
            case "male" -> recHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.MALE);
            case "female" -> recHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.FEMALE);
            case "10", "20", "30", "40" -> {
                LocalDate[] range = getBirthRange(type);
                yield recHistoryRepository.findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, range[0], range[1]);
            }
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }, RecHistory::getTmdbId, RecHistory::getMovieName);
    }

    public List<HistoryResponse.VisitAnalysisDTO> getPersonalRecommendData(String gender, String age) {
        Gender userGender = parseGender(gender);
        LocalDate[] birthRange = getBirthRange(age);
        return getAnalysisData("week", age,
                (start, end) -> visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_GenderAndUser_BirthBetween(
                        start, end, userGender, birthRange[0], birthRange[1]),
                VisitHistory::getTmdbId, VisitHistory::getMovieName);
    }

    private void sendAnalysisData(String eventPrefix, String type, List<HistoryResponse.VisitAnalysisDTO> data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventPrefix + "-" + type).data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public void sendDailyVisitAnalysis(String type) {
        sendAnalysisData("visit-analysis", type, getVisitAnalysisData("day", type));
    }

    public void sendDailyRecommendAnalysis(String type) {
        sendAnalysisData("recommend-analysis", type, getRecommendAnalysisData("day", type));
    }

    public void sendDailyVisitAnalysisToAllTypes() {
        List.of("default", "male", "female", "10", "20", "30", "40")
                .forEach(this::sendDailyVisitAnalysis);
    }

    public void sendDailyRecommendAnalysisToAllTypes() {
        List.of("default", "male", "female", "10", "20", "30", "40")
                .forEach(this::sendDailyRecommendAnalysis);
    }
}
