package popfri.spring.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;

import java.time.LocalDate;

@Getter
@Setter
public class ReviewResponse {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReviewResponseDTO {
        private Long reviewId;
        private Long userId;
        private Long movieId;
        private LocalDate createdAt;
        private String reviewContent;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReviewRequestDTO {
        private Long movieId;
        private String reviewContent;
        private Long userId;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReviewLikeDTO {
        private Long reviewId;
        private Long userId;
    }
}
