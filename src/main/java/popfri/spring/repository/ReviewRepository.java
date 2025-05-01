package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieId(Long movieId);
    @Query(value = """
    SELECT * FROM Review
    WHERE tmdb_id = :movieId
    ORDER BY (like_count * 1.5 - dislike_count) DESC
""", nativeQuery = true)
    List<Review> findReviewsByMovieIdOrderByScore(@Param("movieId") Long movieId);

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    boolean existsByUserAndMovieId(User user, Long movieId);

    List<Review> findByUser(User user);

    //특정 시간 이후의 좋아요순 정렬 된 리뷰 리스트 중 상위 10개
    List<Review> findTop10ByCreatedAtAfter(LocalDateTime localDateTime);
}
