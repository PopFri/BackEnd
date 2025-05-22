package popfri.spring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;
import popfri.spring.web.dto.ReviewResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieId(Long movieId);
    @Query(value = """
    SELECT 
        r.review_id AS reviewId,
        u.user_id AS userId,
        u.user_name AS userName,
        u.user_email AS userEmail,
        u.image_url AS userProfileImage,
        r.tmdb_id AS movieId,
        r.create_at AS createdAt,
        r.text AS reviewContent,
        r.like_count AS likeCount
    FROM Review r
    JOIN User u ON r.user_id = u.user_id
    WHERE r.tmdb_id = :movieId
    ORDER BY (r.like_count * 1.5 - r.dislike_count) DESC
""", countQuery = """
    SELECT COUNT(*) FROM review r WHERE r.tmdb_id = :movieId
""", nativeQuery = true)
    Page<ReviewResponse.ReviewProjectionDTO> findReviewWithUserInfoOrderByScore(@Param("movieId") Long movieId, Pageable pageable);

    @Query(value = """
    SELECT 
        r.review_id AS reviewId,
        u.user_id AS userId,
        u.user_name AS userName,
        u.user_email AS userEmail,
        u.image_url AS userProfileImage,
        r.tmdb_id AS movieId,
        r.create_at AS createdAt,
        r.text AS reviewContent,
        r.like_count AS likeCount
    FROM Review r
    JOIN User u ON r.user_id = u.user_id
    WHERE r.tmdb_id = :movieId
    ORDER BY r.create_at DESC
""", countQuery = """
    SELECT COUNT(*) FROM review r WHERE r.tmdb_id = :movieId
""", nativeQuery = true)
    Page<ReviewResponse.ReviewProjectionDTO> findReviewWithUserInfoOrderByCreatedAt(
            @Param("movieId") Long movieId,
            Pageable pageable
    );

    boolean existsByUserAndMovieId(User user, Long movieId);

    List<Review> findByUser(User user);

    //특정 시간 이후의 좋아요순 정렬 된 리뷰 리스트 중 상위 10개
    List<Review> findTop10ByCreatedAtAfter(LocalDateTime localDateTime);

    Long countByMovieId(Long movieId);
}
