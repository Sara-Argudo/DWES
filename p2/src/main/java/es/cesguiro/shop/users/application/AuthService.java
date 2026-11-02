package es.cesguiro.shop.users.application;

import es.cesguiro.shop.users.domain.Role;
import es.cesguiro.shop.users.domain.User;
import es.cesguiro.shop.users.exception.DuplicateEmailException;
import es.cesguiro.shop.users.exception.InvalidCredentialsException;
import es.cesguiro.shop.users.exception.InvalidRefreshTokenException;
import es.cesguiro.shop.users.infrastructure.persistence.UserDataJpa;
import es.cesguiro.shop.users.infrastructure.security.JwtService;
import es.cesguiro.shop.users.infrastructure.security.JwtService.TokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro, login, refresh y logout.
 * El registro crea siempre USER; el ADMIN solo por semilla (AdminSeeder).
 */
@Service
public class AuthService {

    public record RegisteredUser(long id, String name, String email, String role) {
    }

    private final UserDataJpa users;
    private final JwtService tokens;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDataJpa users, JwtService tokens, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registro público: email único (409 si se repite), rol USER fijo,
     * contraseña almacenada como hash BCrypt.
     */
    @Transactional
    public RegisteredUser register(String name, String email, String rawPassword) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }
        User user = users.save(
                new User(name, email, passwordEncoder.encode(rawPassword), Role.USER)
        );
        return new RegisteredUser(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    /**
     * Login: valida credenciales y emite el par de tokens, que guarda el
     * refresh en BD; por eso la transacción no puede ser read-only.
     */
    @Transactional
    public TokenPair login(String email, String rawPassword) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokens.issue(user.getId(), user.getRole().name());
    }

    /**
     * Refresh: consume y rota el refresh token (queda revoked) y emite un
     * par nuevo para el usuario dueño.
     */
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        long userId = tokens.consumeRefresh(rawRefreshToken);
        User user = users.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);
        return tokens.issue(user.getId(), user.getRole().name());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        tokens.revoke(rawRefreshToken);
    }
}
