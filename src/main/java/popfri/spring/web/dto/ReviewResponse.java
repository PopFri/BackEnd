package popfri.spring.web.dto;

import lombok.*;

import java.time.LocalDateTime;

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
        private LocalDateTime createdAt;
        private String reviewContent;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserReviewListDTO {
        private Long reviewId;
        private Long userId;
        private Long movieId;
        private String reviewContent;
        private LocalDateTime createdAt;
        private String movieName;
        private String posterUrl;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReviewRequestDTO {
        private Long movieId;
        private String reviewContent;
        private String movieName;
        private String posterUrl;
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
