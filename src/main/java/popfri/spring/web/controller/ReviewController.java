package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.domain.User;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.ReviewService;
import popfri.spring.service.UserService;
import popfri.spring.web.dto.ReviewResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
@Tag(name = "Review", description = "리뷰 관련 API입니다.")
public class ReviewController {
    private final JWTUtil jwtUtil;
    private final ReviewService reviewService;
    private final UserService userService;

    // 리뷰 작성
    @PostMapping("/review")
    @Operation(summary = "리뷰 작성", description = "작성된 리뷰 정보 저장.")
    public ApiResponse<ReviewResponse.ReviewResponseDTO> createReview(ReviewResponse.ReviewRequestDTO reviewRequest, HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        return ApiResponse.onSuccess(reviewService.createReview(reviewRequest, user));
    }

    // 리뷰 좋아요
    @PostMapping("/review/like")
    @Operation(summary = "리뷰 좋아요", description = "해당 리뷰에 좋아요 표시.")
    public ApiResponse<Boolean> likeReview(ReviewResponse.ReviewLikeDTO reviewLikeRequest, HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        reviewService.handleReviewReaction(reviewLikeRequest, ReviewService.ReviewActionType.LIKE, user);
        return ApiResponse.onSuccess(true);
    }

    // 리뷰 싫어요
    @PostMapping("/review/dislike")
    @Operation(summary = "리뷰 싫어요", description = "해당 리뷰에 싫어요 표시.")
    public ApiResponse<Boolean> dislikeReview(ReviewResponse.ReviewLikeDTO reviewDislikeRequest, HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        reviewService.handleReviewReaction(reviewDislikeRequest, ReviewService.ReviewActionType.DISLIKE, user);
        return ApiResponse.onSuccess(true);
    }

    // 리뷰 최신순 조회
    @GetMapping("/review/{movieId}/recent")
    @Operation(summary = "리뷰 최신순 조회", description = "해당 영화의 리뷰들을 최신순으로 조회.")
    public ApiResponse<List<ReviewResponse.ReviewResponseDTO>> getReviews(@Parameter String movieId) {
        Long movieIdLong = Long.parseLong(movieId);
        return ApiResponse.onSuccess(reviewService.getReviewsByMovieId(movieIdLong));
    }

    // 리뷰 좋아요 순 조회
    @GetMapping("/review/{movieId}/like")
    @Operation(summary = "리뷰 좋아요 순 조회", description = "해당 영화의 리뷰들을 좋아요와 싫어요의 계산식으로 정렬하여 조회.")
    public ApiResponse<List<ReviewResponse.ReviewResponseDTO>> getReviewsOrderByLike(@Parameter String movieId) {
        Long movieIdLong = Long.parseLong(movieId);
        return ApiResponse.onSuccess(reviewService.getReviewByMovieIdOrderByLike(movieIdLong));
    }

    // 리뷰 삭제
    @DeleteMapping("/review/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "해당 리뷰 삭제")
    public ApiResponse<Boolean> deleteReview(@Parameter String reviewId) {
        Long reviewIdLong = Long.parseLong(reviewId);
        reviewService.deleteReview(reviewIdLong);
        return ApiResponse.onSuccess(true);
    }
}