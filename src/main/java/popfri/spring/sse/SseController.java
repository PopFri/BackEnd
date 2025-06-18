package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import popfri.spring.web.dto.HistoryResponse;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SseController {
    private final SseEmitters sseEmitters;

    @GetMapping("/sse/analysis")
    public ResponseEntity<List<HistoryResponse.VisitAnalysisDTO>> getVisitAnalysisToday(@RequestParam String date, @RequestParam String type) {
        List<HistoryResponse.VisitAnalysisDTO> result = sseEmitters.getVisitAnalysisDataDay(date, type);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/sse/visit-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToVisitAnalysis(
            @RequestParam(defaultValue = "default") String type) {

        SseEmitter emitter = new SseEmitter(0L); // 무제한 유지 (클라이언트가 끊을 때까지)
        sseEmitters.add(emitter);

        // 일정 주기로 데이터 보내기 (예: 10초마다)
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<HistoryResponse.VisitAnalysisDTO> data =
                            sseEmitters.getVisitAnalysisDataDay("day", type);
                    emitter.send(SseEmitter.event()
                            .name("visit-analysis")
                            .data(data));
                } catch (Exception e) {
                    log.error("SSE 전송 중 에러 발생: {}", e.getMessage());
                    emitter.completeWithError(e);
                    sseEmitters.remove(emitter);
                    cancel(); // 타이머 중지
                }
            }
        }, 0, 10_000); // 0초 후 시작, 10초 간격

        // emitter가 완료되면 타이머도 종료
        emitter.onCompletion(() -> {
            sseEmitters.remove(emitter);
            timer.cancel();
            log.info("SSE 연결 종료 (onCompletion)");
        });

        emitter.onTimeout(() -> {
            sseEmitters.remove(emitter);
            timer.cancel();
            log.info("SSE 연결 타임아웃 (onTimeout)");
        });

        return emitter;
    }
}
