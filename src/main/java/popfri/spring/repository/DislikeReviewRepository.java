package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import popfri.spring.domain.DislikeReview;
import popfri.spring.domain.Review;
import popfri.spring.domain.User;

import java.util.List;

@Repository
public interface DislikeReviewRepository extends JpaRepository<DislikeReview, Long> {
    boolean existsByUserAndReview(User user, Review review);
    DislikeReview findByUserAndReview(User user, Review review);
    void deleteAllByReview(Review review);
    List<DislikeReview> findAllByUserAndReview_ReviewIdIn(User user, List<Long> reviewIds);
}
