package es.cesguiro.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Auth: registro (409 por email repetido), login (401 en credenciales
 * inválidas), password NUNCA en claro (hash en base), refresh con
 * rotación, logout que revoca, y roles USER/ADMIN en la práctica real.
 */
class AuthApiTests extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private ResponseEntity<String> post(String path, JsonNode body) {
        return rest.postForEntity(path,
                new org.springframework.http.HttpEntity<>(body.toString(), jsonHeaders()), String.class);
    }

    @Test
    void registerCreatesUserWithHashedPasswordAndDuplicateEmailIsRejected() throws Exception {
        String suffix = suffix();
        var request = json.createObjectNode();
        request.put("name", "New-" + suffix);
        request.put("email", "hashed-" + suffix + "@shop.local");
        request.put("password", "secret-pass-1");

        var created = post("/api/users", request);
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        var user = json.readTree(created.getBody());
        assertThat(user.get("role").asText()).isEqualTo("USER");
        assertThat(user.has("password")).isFalse();
        assertThat(user.has("passwordHash")).isFalse();

        // en base solo está el hash BCrypt, no el secreto en claro
        var row = jdbc.queryForMap("SELECT password_hash FROM users WHERE email = ?", user.get("email").asText());
        String stored = (String) row.get("password_hash");
        assertThat(stored).startsWith("$2");
        assertThat(stored).doesNotContain("secret-pass-1");

        // email duplicado: 409
        var duplicate = post("/api/users", request);
        assertThat(duplicate.getStatusCode().value()).isEqualTo(409);
        assertThat(json.readTree(duplicate.getBody()).get("title").asText()).isEqualTo("Conflict");
    }

    @Test
    void loginFailsWith401OnBadCredentials() throws Exception {
        var user = registerUser("badcred");
        String email = user.get("email").asText();

        var wrongPassword = json.createObjectNode();
        wrongPassword.put("email", email);
        wrongPassword.put("password", "wrong-password");
        assertThat(post("/api/auth/login", wrongPassword).getStatusCode().value()).isEqualTo(401);

        var unknownEmail = json.createObjectNode();
        unknownEmail.put("email", "nobody-" + suffix() + "@shop.local");
        unknownEmail.put("password", "whatever-pass");
        assertThat(post("/api/auth/login", unknownEmail).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void refreshRotatesAndOldTokenNoLongerWorks() throws Exception {
        var user = registerUser("refresher");
        String email = user.get("email").asText();

        var loginRequest = json.createObjectNode();
        loginRequest.put("email", email);
        loginRequest.put("password", "secret-pass-1");
        var tokens = json.readTree(post("/api/auth/login", loginRequest).getBody());
        String refreshToken = tokens.get("refreshToken").asText();

        // refresh válido: par nuevo con tokens distintos (rotación)
        var refreshRequest = json.createObjectNode();
        refreshRequest.put("refreshToken", refreshToken);
        var refreshed = post("/api/auth/refresh", refreshRequest);
        assertThat(refreshed.getStatusCode().value()).isEqualTo(200);
        var newTokens = json.readTree(refreshed.getBody());
        // el refresh rota (aleatorio criptográfico); el access no se compara por
        // igualdad porque dos tokens emitidos en el mismo segundo son idénticos
        assertThat(newTokens.get("refreshToken").asText()).isNotEqualTo(refreshToken);

        // el refresh antiguo se revocó: 401
        var replay = post("/api/auth/refresh", refreshRequest);
        assertThat(replay.getStatusCode().value()).isEqualTo(401);

        // refresh inventado: 401
        var fake = json.createObjectNode();
        fake.put("refreshToken", "f".repeat(64));
        assertThat(post("/api/auth/refresh", fake).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        var user = registerUser("logging-out");
        String email = user.get("email").asText();

        var loginRequest = json.createObjectNode();
        loginRequest.put("email", email);
        loginRequest.put("password", "secret-pass-1");
        String refreshToken = json.readTree(post("/api/auth/login", loginRequest).getBody())
                .get("refreshToken").asText();

        var logoutRequest = json.createObjectNode();
        logoutRequest.put("refreshToken", refreshToken);
        var loggedOut = post("/api/auth/logout", logoutRequest);
        assertThat(loggedOut.getStatusCode().value()).isEqualTo(204);

        // tras logout, el refresh ya no vale: 401
        var afterLogout = post("/api/auth/refresh", logoutRequest);
        assertThat(afterLogout.getStatusCode().value()).isEqualTo(401);
    }
}
