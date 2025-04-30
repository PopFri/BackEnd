package popfri.spring.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import popfri.spring.domain.enums.RecType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class HistoryResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "HISTORY_RES_01 : 기록 영화 응답")
    public static class HistoryMovieResDTO{
        @Schema(description = "TMDB ID", example = "1")
        Integer movieId;
        String movieName;
        String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "HISTORY_RES_02 : 기록 응답")
    public static class HistoryResDTO{
        @Schema(description = "기록 날짜", example = "2025-01-01")
        LocalDate date;

        List<HistoryMovieResDTO> movieList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "HISTORY_RES_03 : 추천 기록 조회 응답")
    public static class RecHistoryGetResDTO{
        @Schema(description = "추천 유형", example = "situation")
        String recType;

        List<HistoryResDTO> historyList;
    }
}
