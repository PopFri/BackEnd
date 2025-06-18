package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import popfri.spring.domain.RecHistory;
import popfri.spring.domain.User;
import popfri.spring.domain.VisitHistory;
import popfri.spring.domain.enums.Gender;
import popfri.spring.domain.enums.RecType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecHistoryRepository extends JpaRepository<RecHistory, Long> {
    List<RecHistory> findByUser(User user);
    List<RecHistory> findDistinctTop10ByUserOrderByUpdatedAtDesc(User user);
    List<RecHistory> findByUserAndRecType(User user, RecType recType);
    Optional<RecHistory> findByRecTypeAndTmdbIdAndUser(RecType recType, Integer tmdbId, User user);
    void deleteByUser(User user);

    List<RecHistory> findAllByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<RecHistory> findAllByUpdatedAtBetweenAndUser_Gender(LocalDateTime start, LocalDateTime end, Gender gender);
    List<RecHistory> findAllByUpdatedAtBetweenAndUser_BirthBetween(LocalDateTime start, LocalDateTime end, LocalDate birthStart, LocalDate birthEnd);

}
