package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import popfri.spring.web.dto.HistoryResponse;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {
    private final SseEmitters sseEmitters;

    @GetMapping("/analysis")
    public ResponseEntity<List<HistoryResponse.VisitAnalysisDTO>> getVisitAnalysisToday(@RequestParam String date, @RequestParam String type) {
        List<HistoryResponse.VisitAnalysisDTO> result = sseEmitters.getVisitAnalysisDataDay(date, type);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        sseEmitters.add(emitter);
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(emitter);
    }

    @GetMapping(value = "/visit-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToVisitAnalysis(
            @RequestParam(defaultValue = "default") String type) {

        SseEmitter emitter = new SseEmitter(60 * 1000L);
        sseEmitters.add(emitter);

        try {
            List<HistoryResponse.VisitAnalysisDTO> data = sseEmitters.getVisitAnalysisDataDay("day", type);
            emitter.send(SseEmitter.event()
                    .name("visit-analysis")
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
            sseEmitters.remove(emitter);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(emitter);
    }
}
