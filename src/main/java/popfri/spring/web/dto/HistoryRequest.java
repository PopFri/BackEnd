package popfri.spring.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class HistoryRequest {
    @Getter
    @Schema(title = "HISTORY_REQ_01 : 방문 기록 저장 요청 DTO")
    public static class AddVisitHisDto{
        @Schema(description = "TMDB ID", example = "1")
        Integer movieId;
        String movieName;
        String imageUrl;
    }
}
