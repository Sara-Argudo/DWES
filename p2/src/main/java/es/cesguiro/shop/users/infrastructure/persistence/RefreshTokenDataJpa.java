package es.cesguiro.shop.users.infrastructure.persistence;

import java.util.Optional;

import es.cesguiro.shop.users.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenDataJpa extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
