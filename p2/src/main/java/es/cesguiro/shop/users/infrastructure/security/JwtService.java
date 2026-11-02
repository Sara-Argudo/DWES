package es.cesguiro.shop.users.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import es.cesguiro.shop.users.domain.RefreshToken;
import es.cesguiro.shop.users.exception.InvalidRefreshTokenException;
import es.cesguiro.shop.users.infrastructure.persistence.RefreshTokenDataJpa;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emisión y verificación de tokens:
 * - access: JWT firmado (HS256) con sub = userId y claim "role".
 * - refresh: token aleatorio; solo se guarda su hash SHA-256. Se rota
 *   (se marca revoked) al consumirse en /refresh y al hacer /logout.
 */
@Service
public class JwtService {

    public record TokenPair(String accessToken, String refreshToken) {
    }

    public record AccessClaims(long userId, String role) {
    }

    private final SecretKey secretKey;
    private final JwtProperties properties;
    private final RefreshTokenDataJpa refreshTokens;
    private final SecureRandom random = new SecureRandom();

    public JwtService(JwtProperties properties, RefreshTokenDataJpa refreshTokens) {
        this.properties = properties;
        this.refreshTokens = refreshTokens;
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Emite un par (access firmado + refresh aleatorio guardado como hash).
     */
    @Transactional
    public TokenPair issue(long userId, String role) {
        Instant now = Instant.now();
        String accessToken = buildAccessToken(userId, role, now);
        String rawRefresh = newRefreshToken();
        Instant expiresAt = now.plus(Duration.ofDays(properties.getRefreshTtlDays()));
        refreshTokens.save(new RefreshToken(sha256Hex(rawRefresh), userId, expiresAt));
        return new TokenPair(accessToken, rawRefresh);
    }

    private String buildAccessToken(long userId, String role, Instant issuedAt) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(java.util.Date.from(issuedAt))
                .expiration(java.util.Date.from(
                        issuedAt.plus(Duration.ofMinutes(properties.getAccessTtlMinutes()))
                ))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Verifica firma y expiración del access token y devuelve userId + role
     * de sus claims; lanza JwtException si el token no es válido.
     */
    public AccessClaims parseAccess(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return new AccessClaims(
                Long.parseLong(claims.getSubject()),
                claims.get("role", String.class)
        );
    }

    /**
     * Consume el refresh token: debe existir por su hash, no estar revocado
     * ni expirado; lo marca revoked (rotación) y devuelve su userId.
     */
    @Transactional
    public long consumeRefresh(String rawToken) {
        RefreshToken stored = refreshTokens.findByTokenHash(sha256Hex(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (stored.isRevoked() || stored.isExpiredAt(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        stored.revoke();
        return stored.getUserId();
    }

    /**
     * Logout: revoca el refresh token si existe (revocar uno ya revocado es idéntico).
     */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokens.findByTokenHash(sha256Hex(rawToken))
                .ifPresent(RefreshToken::revoke);
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
