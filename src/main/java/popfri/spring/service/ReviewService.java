package popfri.spring.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.MovieHandler;
import popfri.spring.apiPayload.exception.handler.ReviewHandler;
import popfri.spring.domain.DislikeReview;
import popfri.spring.domain.LikeReview;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.ReviewActionType;
import popfri.spring.repository.DislikeReviewRepository;
import popfri.spring.repository.LikeReviewRepository;
import popfri.spring.repository.ReviewRepository;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.ReviewResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final LikeReviewRepository likeReviewRepository;
    private final DislikeReviewRepository dislikeReviewRepository;
    private final UserRepository userRepository;
    public ReviewService(ReviewRepository reviewRepository, LikeReviewRepository likeReviewRepository, DislikeReviewRepository dislikeReviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.likeReviewRepository = likeReviewRepository;
        this.dislikeReviewRepository = dislikeReviewRepository;
        this.userRepository = userRepository;
    }

    // 리뷰 생성
    @Transactional
    public ReviewResponse.ReviewResponseDTO createReview(ReviewResponse.ReviewRequestDTO reviewRequest, User user) {

        if(reviewRepository.existsByUserAndMovieId(user, reviewRequest.getMovieId())) {
            throw new ReviewHandler(ErrorStatus._REVIEW_ALREADY_EXIST);
        } else {
            Review review = Review.builder()
                    .reviewContent(reviewRequest.getReviewContent())
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .movieId(reviewRequest.getMovieId())
                    .movieName(reviewRequest.getMovieName())
                    .posterUrl(reviewRequest.getPosterUrl())
                    .likeCount(0)
                    .dislikeCount(0)
                    .build();
            reviewRepository.save(review);
            return ReviewResponse.ReviewResponseDTO.builder()
                    .reviewId(review.getReviewId())
                    .userId(review.getUser().getUserId())
                    .movieId(review.getMovieId())
                    .createdAt(review.getCreatedAt())
                    .reviewContent(review.getReviewContent())
                    .build();
        }
    }

    // 영화 별 리뷰 조회 최신순
    public ReviewResponse.ReviewListDTO getReviewsByMovieId(Long movieId, int page, User user) {
        Pageable pageable = PageRequest.of(page - 1, 20);
        Page<ReviewResponse.ReviewProjectionDTO> reviewPage = reviewRepository.findReviewWithUserInfoOrderByCreatedAt(movieId, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(ReviewResponse.ReviewProjectionDTO::getReviewId)
                .toList();

        Map<Long, String> statusMap = new HashMap<>();
        likeReviewRepository.findAllByUserAndReview_ReviewIdIn(user, reviewIds)
                .forEach(lr -> statusMap.put(lr.getReview().getReviewId(), "like"));
        dislikeReviewRepository.findAllByUserAndReview_ReviewIdIn(user, reviewIds)
                .forEach(dr -> statusMap.put(dr.getReview().getReviewId(), "dislike"));

        List<ReviewResponse.ReviewResponseDTO> reviewDtoList = reviewPage.getContent().stream()
                .map(review -> ReviewResponse.ReviewResponseDTO.builder()
                        .reviewId(review.getReviewId())
                        .movieId(review.getMovieId())
                        .userId(review.getUserId())
                        .userName(review.getUserName())
                        .userEmail(review.getUserEmail())
                        .userImageUrl(review.getUserProfileImage())
                        .createdAt(review.getCreatedAt())
                        .reviewContent(review.getReviewContent())
                        .likeCount(review.getLikeCount())
                        .likeStatus(statusMap.get(review.getReviewId()))
                        .build()
                ).toList();

        return ReviewResponse.ReviewListDTO.builder()
                .hasNext(reviewPage.hasNext())
                .totalReview(reviewPage.getTotalElements())
                .totalPage(reviewPage.getTotalPages())
                .reviews(reviewDtoList)
                .build();
    }

    // 영화 별 리뷰 조회 좋아요순
    public ReviewResponse.ReviewListDTO getReviewByMovieIdOrderByLike(Long movieId, int page, User user) {
        Pageable pageable = PageRequest.of(page - 1, 20);
        Page<ReviewResponse.ReviewProjectionDTO> reviewPage = reviewRepository.findReviewWithUserInfoOrderByScore(movieId, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(ReviewResponse.ReviewProjectionDTO::getReviewId)
                .toList();

        Map<Long, String> statusMap = new HashMap<>();
        likeReviewRepository.findAllByUserAndReview_ReviewIdIn(user, reviewIds)
                .forEach(lr -> statusMap.put(lr.getReview().getReviewId(), "like"));
        dislikeReviewRepository.findAllByUserAndReview_ReviewIdIn(user, reviewIds)
                .forEach(dr -> statusMap.put(dr.getReview().getReviewId(), "dislike"));

        List<ReviewResponse.ReviewResponseDTO> reviewDtoList = reviewPage.getContent().stream()
                .map(review -> ReviewResponse.ReviewResponseDTO.builder()
                            .reviewId(review.getReviewId())
                            .movieId(review.getMovieId())
                            .userId(review.getUserId())
                            .userName(review.getUserName())
                            .userEmail(review.getUserEmail())
                            .userImageUrl(review.getUserProfileImage())
                            .createdAt(review.getCreatedAt())
                            .reviewContent(review.getReviewContent())
                            .likeCount(review.getLikeCount())
                            .likeStatus(statusMap.get(review.getReviewId()))
                            .build()
                ).toList();

        return ReviewResponse.ReviewListDTO.builder()
                .hasNext(reviewPage.hasNext())
                .totalReview(reviewPage.getTotalElements())
                .totalPage(reviewPage.getTotalPages())
                .reviews(reviewDtoList)
                .build();
    }

    // 리뷰 좋아요/싫어요 처리
    @Transactional
    public void handleReviewReaction(ReviewResponse.ReviewLikeDTO reviewLikeRequest, ReviewActionType actionType, User user) {
        Long reviewId = reviewLikeRequest.getReviewId();

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
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewHandler(ErrorStatus._REVIEW_NOT_EXIST));
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new ReviewHandler(ErrorStatus._REVIEW_NOT_YOUR);
        } else {
            likeReviewRepository.deleteAllByReview(review);
            dislikeReviewRepository.deleteAllByReview(review);
            reviewRepository.delete(review);
        }
    }

    // 유저 리뷰 조회
    public List<ReviewResponse.UserReviewListDTO> getReviewsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MovieHandler(ErrorStatus._USER_NOT_EXIST));
        List<Review> reviews = reviewRepository.findByUser(user);
        return reviews.stream()
                .map(review -> ReviewResponse.UserReviewListDTO.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUser().getUserId())
                        .movieId(review.getMovieId())
                        .movieName(review.getMovieName())
                        .posterUrl(review.getPosterUrl())
                        .createdAt(review.getCreatedAt())
                        .reviewContent(review.getReviewContent())
                        .likeCount(review.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }
}
