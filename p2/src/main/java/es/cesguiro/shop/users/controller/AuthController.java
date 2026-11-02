package es.cesguiro.shop.users.controller;

import es.cesguiro.shop.users.application.AuthService;
import es.cesguiro.shop.users.infrastructure.security.JwtService.TokenPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Endpoints de autenticación y registro del módulo users:
 * - POST /api/auth/login, /api/auth/refresh, /api/auth/logout (públicos)
 * - POST /api/users (registro público, rol USER) — 409 si email repetido
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(
            @NotBlank(message = "name is required") @Size(max = 80) String name,
            @NotBlank(message = "email is required") @Email @Size(max = 120) String email,
            @NotBlank(message = "password is required")
            @Size(min = 8, message = "password must be at least 8 characters") String password
    ) {
    }

    public record UserDto(long id, String name, String email, String role) {
    }

    public record LoginRequest(
            @NotBlank(message = "email is required") @Email String email,
            @NotBlank(message = "password is required") String password
    ) {
    }

    public record RefreshRequest(@NotBlank(message = "refreshToken is required") String refreshToken) {
    }

    public record TokensDto(String accessToken, String refreshToken) {
        static TokensDto from(TokenPair pair) {
            return new TokensDto(pair.accessToken(), pair.refreshToken());
        }
    }

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokensDto login(@Valid @RequestBody LoginRequest request) {
        return TokensDto.from(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public TokensDto refresh(@Valid @RequestBody RefreshRequest request) {
        return TokensDto.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
