package es.cesguiro.shop.users.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Refresh token: se guarda SOLO el hash (SHA-256), nunca el token en claro.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    protected RefreshToken() {
    }

    public RefreshToken(String tokenHash, Long userId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public boolean isExpiredAt(Instant now) {
        return expiresAt.isBefore(now);
    }

    public void revoke() {
        this.revoked = true;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
