package popfri.spring.web.dto;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReviewResponse {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ReviewListDTO {
        private boolean hasNext;
        private Long totalReview;
        private int totalPage;
        private List<ReviewResponseDTO> reviews;
    }

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
        private String userName;
        private String userImageUrl;
        private String userEmail;
        private Integer likeCount;
        private String likeStatus;
    }


    @NoArgsConstructor
    @Getter
    @Setter
    public static class ReviewProjectionDTO {
        private Long reviewId;
        private Long userId;
        private String userName;
        private String userEmail;
        private String userProfileImage;
        private Long movieId;
        private LocalDateTime createdAt;
        private String reviewContent;
        private Integer likeCount;

        public ReviewProjectionDTO(Long reviewId, Long userId, String userName, String userEmail, String userProfileImage, Long movieId, Timestamp createdAt, String reviewContent, Integer likeCount) {
            this.reviewId = reviewId;
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.userProfileImage = userProfileImage;
            this.movieId = movieId;
            this.createdAt = createdAt.toLocalDateTime();
            this.reviewContent = reviewContent;
            this.likeCount = likeCount;
        }
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
        private Integer likeCount;
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
