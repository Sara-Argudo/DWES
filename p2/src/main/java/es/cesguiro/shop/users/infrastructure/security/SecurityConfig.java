package es.cesguiro.shop.users.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad de la API, según la tabla de roles de la especificación:
 * - GET de catálogos y productos: público.
 * - Escritura de catálogos y productos: solo ADMIN.
 * - Registro y auth (login/refresh/logout): público.
 * - Pedidos: autenticados; la pertenencia la decide el módulo orders.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, e) ->
                                writeProblem(response, 401, "Unauthorized", "Missing or invalid token"))
                        .accessDeniedHandler((request, response, e) ->
                                writeProblem(response, 403, "Forbidden", "Access denied")))
                .authorizeHttpRequests(auth -> auth
                        // Lecturas públicas del catálogo
                        .requestMatchers(HttpMethod.GET,
                                "/api/catalogs", "/api/catalogs/*",
                                "/api/catalogs/*/products", "/api/products/*").permitAll()
                        // Registro y autenticación
                        .requestMatchers(HttpMethod.POST,
                                "/api/users", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        // Escritura de catálogo solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/catalogs", "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/catalogs/*", "/api/products/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/catalogs/*", "/api/products/*").hasRole("ADMIN")
                        // los dispatchers de error de Spring (404 de rutas, conversiones...) públicos
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        // Pedidos y todo lo demás: autenticados
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Errores de seguridad también en RFC 9457 (Problem), como el resto de la API.
     */
    private void writeProblem(HttpServletResponse response, int status, String title, String detail)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"title":"%s","status":%d,"detail":"%s"}
                """.formatted(title, status, detail));
    }
}
