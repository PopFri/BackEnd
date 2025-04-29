package popfri.spring.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.MovieHandler;
import popfri.spring.apiPayload.exception.handler.ReviewHandler;
import popfri.spring.domain.DislikeReview;
import popfri.spring.domain.LikeReview;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;
import popfri.spring.repository.DislikeReviewRepository;
import popfri.spring.repository.LikeReviewRepository;
import popfri.spring.repository.ReviewRepository;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.MovieResponse;
import popfri.spring.web.dto.ReviewResponse;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final LikeReviewRepository likeReviewRepository;
    private final DislikeReviewRepository dislikeReviewRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;
    public ReviewService(ReviewRepository reviewRepository, LikeReviewRepository likeReviewRepository, DislikeReviewRepository dislikeReviewRepository, UserRepository userRepository, MovieService movieService) {
        this.reviewRepository = reviewRepository;
        this.likeReviewRepository = likeReviewRepository;
        this.dislikeReviewRepository = dislikeReviewRepository;
        this.userRepository = userRepository;
        this.movieService = movieService;
    }

    // 리뷰 생성
    @Transactional
    public void createReview(ReviewResponse.ReviewRequestDTO reviewRequest) {
        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new MovieHandler(ErrorStatus._USER_NOT_EXIST));

        if(reviewRepository.existsByUserAndMovieId(user, reviewRequest.getMovieId())) {
            throw new ReviewHandler(ErrorStatus._REVIEW_ALREADY_EXIST);
        } else {
            MovieResponse.MovieReviewDTO movieReviewDTO = movieService.loadMovieReview(reviewRequest.getMovieId().toString());

            Review review = Review.builder()
                    .reviewContent(reviewRequest.getReviewContent())
                    .user(user)
                    .createdAt(LocalDate.now())
                    .movieId(reviewRequest.getMovieId())
                    .movieName(movieReviewDTO.getMovieName())
                    .posterUrl(movieReviewDTO.getPosterUrl())
                    .likeCount(0)
                    .dislikeCount(0)
                    .likeReview(new ArrayList<>())
                    .dislikeReview(new ArrayList<>())
                    .build();
            reviewRepository.save(review);
        }
    }

    // 영화 별 리뷰 조회 최신순
    public List<ReviewResponse.ReviewResponseDTO> getReviewsByMovieId(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
        return reviews.stream()
                .map(review -> ReviewResponse.ReviewResponseDTO.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUser().getUserId())
                        .movieId(review.getMovieId())
                        .createdAt(review.getCreatedAt())
                        .reviewContent(review.getReviewContent())
                        .build())
                .collect(Collectors.toList());
    }

    // 영화 별 리뷰 조회 좋아요순
    public List<ReviewResponse.ReviewResponseDTO> getReviewByMovieIdOrderByLike(Long movieId) {
        List<Review> reviews = reviewRepository.findReviewsByMovieIdOrderByScore(movieId);

        return reviews.stream()
                .map(review -> ReviewResponse.ReviewResponseDTO.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUser().getUserId())
                        .movieId(review.getMovieId())
                        .createdAt(review.getCreatedAt())
                        .reviewContent(review.getReviewContent())
                        .build())
                .collect(Collectors.toList());
    }

    public enum ReviewActionType {
        LIKE, DISLIKE
    }

    public boolean hasUserLikedReview(User user, Review review) {
        return likeReviewRepository.existsByUserAndReview(user, review);
    }

    public boolean hasUserDislikedReview(User user, Review review) {
        return dislikeReviewRepository.existsByUserAndReview(user, review);
    }

    @Transactional
    public void handleReviewReaction(ReviewResponse.ReviewLikeDTO reviewLikeRequest, ReviewActionType actionType) {
        Long userId = reviewLikeRequest.getUserId();
        Long reviewId = reviewLikeRequest.getReviewId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MovieHandler(ErrorStatus._USER_NOT_EXIST));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus._REVIEW_NOT_EXIST));

        boolean liked = likeReviewRepository.existsByUserAndReview(user, review);
        boolean disliked = dislikeReviewRepository.existsByUserAndReview(user, review);

        switch (actionType) {
            case LIKE -> {
                if (liked) {
                    LikeReview like = likeReviewRepository.findByUserAndReview(user, review);
                    likeReviewRepository.delete(like);
                    review.removeLike();
                } else {
                    if (disliked) {
                        DislikeReview dislike = dislikeReviewRepository.findByUserAndReview(user, review);
                        dislikeReviewRepository.delete(dislike);
                        review.removeDislike();
                    }
                    likeReviewRepository.save(LikeReview.builder()
                            .user(user)
                            .review(review)
                            .build());
                    review.addLike();
                }
            }
            case DISLIKE -> {
                if (disliked) {
                    DislikeReview dislike = dislikeReviewRepository.findByUserAndReview(user, review);
                    dislikeReviewRepository.delete(dislike);
                    review.removeDislike();
                } else {
                    if (liked) {
                        LikeReview like = likeReviewRepository.findByUserAndReview(user, review);
                        likeReviewRepository.delete(like);
                        review.removeLike();
                    }
                    dislikeReviewRepository.save(DislikeReview.builder()
                            .user(user)
                            .review(review)
                            .build());
                    review.addDislike();
                }
            }
        }
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus._REVIEW_NOT_EXIST));
        reviewRepository.delete(review);
    }
}
