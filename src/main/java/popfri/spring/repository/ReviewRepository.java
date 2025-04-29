package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;

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
}
