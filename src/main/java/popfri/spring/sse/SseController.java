package popfri.spring.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.web.dto.HistoryResponse;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {
    private final SseEmitters sseEmitters;

    @GetMapping("/analysis/visit")
    public ApiResponse<List<HistoryResponse.VisitAnalysisDTO>> getVisitAnalysisToday(@RequestParam String date, @RequestParam String type) {
        List<HistoryResponse.VisitAnalysisDTO> result = sseEmitters.getVisitAnalysisData(date, type);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/analysis/recommend")
    public ApiResponse<List<HistoryResponse.VisitAnalysisDTO>> getRecommendAnalysisToday(@RequestParam String date, @RequestParam String type) {
        List<HistoryResponse.VisitAnalysisDTO> result = sseEmitters.getRecommendAnalysisData(date, type);
        return ApiResponse.onSuccess(result);
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
            List<HistoryResponse.VisitAnalysisDTO> data = sseEmitters.getVisitAnalysisData("day", type);
            emitter.send(SseEmitter.event()
                    .name("visit-analysis-" + type)
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
            sseEmitters.remove(emitter);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(emitter);
    }

    @GetMapping(value = "/recommend-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToRecommendAnalysis(
            @RequestParam(defaultValue = "default") String type) {

        SseEmitter emitter = new SseEmitter(60 * 1000L);
        sseEmitters.add(emitter);

        try {
            List<HistoryResponse.VisitAnalysisDTO> data = sseEmitters.getRecommendAnalysisData("day", type);
            emitter.send(SseEmitter.event()
                    .name("recommend-analysis-" + type)
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
            sseEmitters.remove(emitter);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(emitter);
    }
}
