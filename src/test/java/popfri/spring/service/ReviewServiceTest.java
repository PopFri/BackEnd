package popfri.spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;
import popfri.spring.repository.ReviewRepository;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.ReviewResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired private ReviewService reviewService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private UserRepository userRepository;

    private User user;
    private Review review;

    @BeforeEach
    void setup() {
        user = User.builder()
                .userEmail("test@user.com")
                .userName("tester")
                .imageUrl("img.png")
                .provideId("pid")
                .loginType("kakao")
                .build();
        userRepository.save(user);

        review = Review.builder()
                .reviewContent("굿무비")
                .user(user)
                .createdAt(LocalDateTime.now())
                .movieId(1L)
                .movieName("테스트영화")
                .posterUrl("poster.png")
                .likeCount(0)
                .dislikeCount(0)
                .build();
        reviewRepository.save(review);
    }

    private ReviewResponse.ReviewLikeDTO createLikeDto() {
        return ReviewResponse.ReviewLikeDTO.builder()
                .userId(user.getUserId())
                .reviewId(review.getReviewId())
                .build();
    }

    @Test
    @DisplayName("1. 좋아요 처음 누름")
    void like_first_click() {
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(1);
        assertThat(updated.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("2. 좋아요 다시 누르면 취소됨")
    void like_click_twice_removes_like() {
        // 첫 클릭
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.LIKE);
        // 두 번째 클릭
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
        assertThat(updated.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("3. 싫어요 상태에서 좋아요 누르면 싫어요 취소되고 좋아요 반영")
    void dislike_to_like_transition() {
        // 싫어요 먼저
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.DISLIKE);
        // 좋아요 전환
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(1);
        assertThat(updated.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("4. 싫어요 처음 누름")
    void dislike_first_click() {
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
        assertThat(updated.getDislikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("5. 싫어요 다시 누르면 취소됨")
    void dislike_click_twice_removes_dislike() {
        // 첫 클릭
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.DISLIKE);
        // 두 번째 클릭
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
        assertThat(updated.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("6. 좋아요 상태에서 싫어요 누르면 좋아요 취소되고 싫어요 반영")
    void like_to_dislike_transition() {
        // 좋아요 먼저
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.LIKE);
        // 싫어요 전환
        reviewService.handleReviewReaction(createLikeDto(), ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
        assertThat(updated.getDislikeCount()).isEqualTo(1);
    }
}