package es.cesguiro.shop.users.controller;

import es.cesguiro.shop.users.application.AuthService;
import es.cesguiro.shop.users.controller.AuthController.RegisterRequest;
import es.cesguiro.shop.users.controller.AuthController.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Registro público en POST /api/users: siempre rol USER, 409 si el email
 * ya está registrado. La contraseña se guarda hasheada.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request,
                                            UriComponentsBuilder uri) {
        AuthService.RegisteredUser registered =
                authService.register(request.name(), request.email(), request.password());
        UserDto body = new UserDto(
                registered.id(),
                registered.name(),
                registered.email(),
                registered.role()
        );
        var location = uri.path("/api/users/{id}").buildAndExpand(registered.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }
}
