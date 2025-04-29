package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.service.ReviewService;
import popfri.spring.web.dto.ReviewResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
@Tag(name = "Review", description = "리뷰 관련 API입니다.")
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/review")
    public void createReview(ReviewResponse.ReviewRequestDTO reviewRequest) {
        reviewService.createReview(reviewRequest);
    }

    // 리뷰 좋아요
    @PostMapping("/review/like")
    public void likeReview(ReviewResponse.ReviewLikeDTO reviewLikeRequest) {
        reviewService.handleReviewReaction(reviewLikeRequest, ReviewService.ReviewActionType.LIKE);
    }

    // 리뷰 싫어요
    @PostMapping("/review/dislike")
    public void dislikeReview(ReviewResponse.ReviewLikeDTO reviewDislikeRequest) {
        reviewService.handleReviewReaction(reviewDislikeRequest, ReviewService.ReviewActionType.DISLIKE);
    }

    // 리뷰 최신순 조회
    @GetMapping("/review/{movieId}")
    public List<ReviewResponse.ReviewResponseDTO> getReviews(@Parameter String movieId) {
        Long movieIdLong = Long.parseLong(movieId);
        return reviewService.getReviewsByMovieId(movieIdLong);
    }

    // 리뷰 좋아요 순 조회
    @GetMapping("/review/{movieId}/like")
    public List<ReviewResponse.ReviewResponseDTO> getReviewsOrderByLike(@Parameter String movieId) {
        Long movieIdLong = Long.parseLong(movieId);
        return reviewService.getReviewByMovieIdOrderByLike(movieIdLong);
    }

    // 리뷰 삭제
    @DeleteMapping("/review/{reviewId}")
    public void deleteReview(@Parameter String reviewId) {
        Long reviewIdLong = Long.parseLong(reviewId);
        reviewService.deleteReview(reviewIdLong);
    }
}