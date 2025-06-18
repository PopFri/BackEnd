package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    public List<HistoryResponse.VisitAnalysisDTO> getVisitAnalysisDataDay(String date, String type) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate today = LocalDate.now();
        LocalDate birthStart;
        LocalDate birthEnd;
        int currentYear = today.getYear();

        switch (date.toLowerCase()) {
            case "day":
                start = today.atStartOfDay();
                end = today.atTime(LocalTime.MAX);
                break;
            case "week":
                DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
                start = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).atStartOfDay();
                end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
                break;
            case "month":
                start = today.withDayOfMonth(1).atStartOfDay();
                end = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + date);
        }

        List<VisitHistory> visitHistories =  new ArrayList<>();

        switch (type) {
            case "default":
                visitHistories = visitHistoryRepository.findAllByUpdatedAtBetween(start, end);
                break;
            case "male":
                visitHistories = visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.MALE);
                break;
            case "female":
                visitHistories = visitHistoryRepository.findAllByUpdatedAtBetweenAndUser_Gender(start, end, Gender.FEMALE);
                break;
            case "10":
                birthStart = LocalDate.of(currentYear - 19, 1, 1);
                birthEnd = LocalDate.of(currentYear - 10, 12, 31);
                visitHistories = visitHistoryRepository
                        .findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, birthStart, birthEnd);
                break;
            case "20":
                birthStart = LocalDate.of(currentYear - 29, 1, 1);
                birthEnd = LocalDate.of(currentYear - 20, 12, 31);
                visitHistories = visitHistoryRepository
                        .findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, birthStart, birthEnd);
                break;
            case "30":
                birthStart = LocalDate.of(currentYear - 39, 1, 1);
                birthEnd = LocalDate.of(currentYear - 30, 12, 31);
                visitHistories = visitHistoryRepository
                        .findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, birthStart, birthEnd);
                break;
            case "40":
                birthStart = LocalDate.of(currentYear - 200, 1, 1);
                birthEnd = LocalDate.of(currentYear - 40, 12, 31);
                visitHistories = visitHistoryRepository
                        .findAllByUpdatedAtBetweenAndUser_BirthBetween(start, end, birthStart, birthEnd);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 유형: " + date);
        }
        return visitHistories.stream()
                .collect(Collectors.groupingBy(VisitHistory::getTmdbId))
                .entrySet().stream()
                .map(entry -> {
                    Integer tmdbId = entry.getKey();
                    List<VisitHistory> group = entry.getValue();
                    VisitHistory sample = group.get(0);
                    return new HistoryResponse.VisitAnalysisDTO(
                            tmdbId,
                            sample.getMovieName(),
                            group.size()
                    );
                })
                .sorted(Comparator.comparingLong(HistoryResponse.VisitAnalysisDTO::getCount).reversed())
                .collect(Collectors.toList());
    }

    public void sendDailyVisitAnalysis(String type) {
        List<HistoryResponse.VisitAnalysisDTO> data = getVisitAnalysisDataDay("day", type);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("visit-analysis")
                        .data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
