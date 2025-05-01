package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.RecType;

import java.util.List;

public interface RecHistoryRepository extends JpaRepository<RecHistory, Long> {
    List<RecHistory> findByUser(User user);
    List<RecHistory> findDistinctTop10ByUserOrderByCreatedAtDesc(User user);
    List<RecHistory> findByUserAndRecType(User user, RecType recType);
    void deleteByUser(User user);
}
