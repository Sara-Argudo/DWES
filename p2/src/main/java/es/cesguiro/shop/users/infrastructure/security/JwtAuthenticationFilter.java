package es.cesguiro.shop.users.infrastructure.security;

import java.io.IOException;
import java.util.List;

import es.cesguiro.shop.users.infrastructure.security.JwtService.AccessClaims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro stateless: lee el Bearer token y, si es un access token válido,
 * instala el Authentication con principal = userId y rol ROLE_x. El
 * SecurityContext queda vacío si no hay token: las reglas por rutas
 * deciden 401/403 después.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                AccessClaims claims = jwtService.parseAccess(header.substring(7));
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.userId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
