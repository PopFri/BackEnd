package popfri.spring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import popfri.spring.domain.*;
import popfri.spring.repository.*;
import popfri.spring.web.dto.ReviewResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private LikeReviewRepository likeReviewRepository;
    @Autowired
    private DislikeReviewRepository dislikeReviewRepository;

    private User user;
    private Long movieId;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .userEmail("test@user.com")
                .userName("Test User")
                .provideId("kakao")
                .loginType("kakao")
                .imageUrl("https://example.com/profile.jpg")
                .build());
        movieId = 123L;
    }

    @Test
    @DisplayName("createReview - 리뷰 생성 성공")
    void testCreateReview() {
        ReviewResponse.ReviewRequestDTO request = ReviewResponse.ReviewRequestDTO.builder()
                .userId(user.getUserId())
                .movieId(movieId)
                .reviewContent("정말 재밌는 영화였습니다!")
                .build();

        reviewService.createReview(request);

        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getReviewContent()).isEqualTo("정말 재밌는 영화였습니다!");
    }

    @Test
    @DisplayName("getReviewsByMovieId - 최신순 정렬")
    void testGetReviewsByMovieId() {
        for (int i = 0; i < 3; i++) {
            reviewRepository.save(Review.builder()
                    .user(user)
                    .movieId(movieId)
                    .reviewContent("리뷰" + i)
                    .createdAt(LocalDate.now().minusDays(i))
                    .likeCount(0)
                    .dislikeCount(0)
                    .likeReview(new ArrayList<>())
                    .dislikeReview(new ArrayList<>())
                    .build());
        }

        List<ReviewResponse.ReviewResponseDTO> result = reviewService.getReviewsByMovieId(movieId);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReviewContent()).isEqualTo("리뷰0");
    }

    @Test
    @DisplayName("getReviewByMovieIdOrderByLike - 점수순 정렬")
    void testGetReviewByScore() {
        Review low = reviewRepository.save(Review.builder()
                .user(user).movieId(movieId).reviewContent("low")
                .likeCount(1).dislikeCount(2).createdAt(LocalDate.now())
                .likeReview(new ArrayList<>()).dislikeReview(new ArrayList<>()).build());

        Review high = reviewRepository.save(Review.builder()
                .user(user).movieId(movieId).reviewContent("high")
                .likeCount(5).dislikeCount(0).createdAt(LocalDate.now())
                .likeReview(new ArrayList<>()).dislikeReview(new ArrayList<>()).build());

        List<ReviewResponse.ReviewResponseDTO> result = reviewService.getReviewByMovieIdOrderByLike(movieId);
        assertThat(result.get(0).getReviewContent()).isEqualTo("high");
    }

    @Test
    @DisplayName("handleReviewReaction - 좋아요 추가")
    void testLikeReview() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(1);
        assertThat(likeReviewRepository.existsByUserAndReview(user, updated)).isTrue();
    }

    @Test
    @DisplayName("handleReviewReaction - 좋아요 중복 누르기")
    void testLikeToggle() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.LIKE);
        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("handleReviewReaction - 싫어요 추가")
    void testDislikeReview() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getDislikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("handleReviewReaction - 싫어요 중복 누르기")
    void testDislikeToggle() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.DISLIKE);
        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("handleReviewReaction - 좋아요 → 싫어요")
    void testLikeToDislike() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.LIKE);
        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.DISLIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getLikeCount()).isZero();
        assertThat(updated.getDislikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("handleReviewReaction - 싫어요 → 좋아요")
    void testDislikeToLike() {
        Review review = reviewRepository.save(makeReview());
        ReviewResponse.ReviewLikeDTO dto = makeLikeDTO(review);

        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.DISLIKE);
        reviewService.handleReviewReaction(dto, ReviewService.ReviewActionType.LIKE);

        Review updated = reviewRepository.findById(review.getReviewId()).orElseThrow();
        assertThat(updated.getDislikeCount()).isZero();
        assertThat(updated.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteReview - 리뷰 삭제")
    void testDeleteReview() {
        Review review = reviewRepository.save(makeReview());
        Long id = review.getReviewId();

        reviewService.deleteReview(id);

        assertThat(reviewRepository.findById(id)).isEmpty();
    }

    private Review makeReview() {
        return Review.builder()
                .user(user)
                .movieId(movieId)
                .reviewContent("test")
                .createdAt(LocalDate.now())
                .likeCount(0)
                .dislikeCount(0)
                .likeReview(new ArrayList<>())
                .dislikeReview(new ArrayList<>())
                .build();
    }

    private ReviewResponse.ReviewLikeDTO makeLikeDTO(Review review) {
        return ReviewResponse.ReviewLikeDTO.builder()
                .userId(user.getUserId())
                .reviewId(review.getReviewId())
                .build();
    }
}
