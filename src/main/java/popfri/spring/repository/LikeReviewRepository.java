package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import popfri.spring.domain.LikeReview;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;

import java.util.List;

@Repository
public interface LikeReviewRepository extends JpaRepository<LikeReview, Long> {
    boolean existsByUserAndReview(User user, Review review);
    LikeReview findByUserAndReview(User user, Review review);
    void deleteAllByReview(Review review);
    List<LikeReview> findAllByUserAndReview_ReviewIdIn(User user, List<Long> reviewIds);
}
