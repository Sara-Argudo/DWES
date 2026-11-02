package es.cesguiro.shop.users.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Propiedades de los tokens, con valores de desarrollo por defecto.
 * En producción se sobreescriben por variables de entorno (JWT_SECRET...).
 */
@Component
public class JwtProperties {

    @Value("${shop.jwt.secret}")
    private String secret;

    @Value("${shop.jwt.access-ttl-minutes}")
    private long accessTtlMinutes;

    @Value("${shop.jwt.refresh-ttl-days}")
    private long refreshTtlDays;

    public String getSecret() {
        return secret;
    }

    public long getAccessTtlMinutes() {
        return accessTtlMinutes;
    }

    public long getRefreshTtlDays() {
        return refreshTtlDays;
    }
}
