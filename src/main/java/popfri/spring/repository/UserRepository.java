package popfri.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import popfri.spring.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByProvideId(String provideId);
}
