package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import popfri.spring.domain.User;
import popfri.spring.domain.VisitHistory;
import popfri.spring.domain.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitHistoryRepository extends JpaRepository<VisitHistory, Long> {
    Optional<VisitHistory> findByUserAndTmdbId(User user, Integer tmdbId);
    List<VisitHistory> findByUserOrderByUpdatedAtDesc(User user);
    void deleteByUser(User user);
    List<VisitHistory> findAllByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<VisitHistory> findAllByUpdatedAtBetweenAndUser_Gender(LocalDateTime start, LocalDateTime end, Gender gender);
    List<VisitHistory> findAllByUpdatedAtBetweenAndUser_BirthBetween(LocalDateTime start, LocalDateTime end, LocalDate birthStart, LocalDate birthEnd);
    List<VisitHistory> findAllByUpdatedAtBetweenAndUser_GenderAndUser_BirthBetween(
            LocalDateTime start, LocalDateTime end,
            Gender gender,
            LocalDate birthStart, LocalDate birthEnd
    );
}
