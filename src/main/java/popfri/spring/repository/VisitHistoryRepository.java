package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import popfri.spring.domain.User;
import popfri.spring.domain.VisitHistory;

import java.util.List;
import java.util.Optional;

public interface VisitHistoryRepository extends JpaRepository<VisitHistory, Long> {
    Optional<VisitHistory> findByUserAndTmdbId(User user, Integer tmdbId);
    List<VisitHistory> findByUserOrderByUpdatedAtDesc(User user);
    void deleteByUser(User user);
}
