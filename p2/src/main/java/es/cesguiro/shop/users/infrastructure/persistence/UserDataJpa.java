package es.cesguiro.shop.users.infrastructure.persistence;

import java.util.Optional;

import es.cesguiro.shop.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataJpa extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
