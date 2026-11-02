package es.cesguiro.shop.users.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.cesguiro.shop.users.domain.Role;
import es.cesguiro.shop.users.domain.User;
import es.cesguiro.shop.users.infrastructure.persistence.UserDataJpa;

/**
 * Semilla del rol ADMIN: se crea solo si no existe, con los datos de
 * configuración (propiedades que se sobre escriben por variables de
 * entorno). La contraseña se guarda HASHEADA con BCrypt, nunca en claro.
 * El registro público no permite obtener el rol ADMIN.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private final UserDataJpa users;
    private final PasswordEncoder passwordEncoder;

    @Value("${shop.admin.name}")
    private String adminName;

    @Value("${shop.admin.email}")
    private String adminEmail;

    @Value("${shop.admin.password}")
    private String adminPassword;

    public AdminSeeder(UserDataJpa users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
        users.save(new User(
                adminName,
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Role.ADMIN
        ));
    }
}
