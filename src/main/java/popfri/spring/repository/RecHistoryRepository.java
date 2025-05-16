package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.RecType;

import java.util.List;
import java.util.Optional;

public interface RecHistoryRepository extends JpaRepository<RecHistory, Long> {
    List<RecHistory> findByUser(User user);
    List<RecHistory> findDistinctTop10ByUserOrderByUpdatedAtDesc(User user);
    List<RecHistory> findByUserAndRecType(User user, RecType recType);
    Optional<RecHistory> findByRecTypeAndTmdbIdAndUser(RecType recType, Integer tmdbId, User user);
    void deleteByUser(User user);
}
