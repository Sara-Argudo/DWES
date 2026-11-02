package es.cesguiro.shop;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * Base de las pruebas de integración: un solo contenedor MariaDB 12
 * (la misma imagen que el compose), con el esquema y la semilla de db/init.sql.
 * Los emails/nombres se sufijan con UUID para no chocar entre clases.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest {

    static final org.testcontainers.containers.MariaDBContainer<?> DB =
            new org.testcontainers.containers.MariaDBContainer<>("mariadb:12")
                    .withDatabaseName("shop")
                    .withUsername("shop")
                    .withPassword("shop")
                    .withInitScript("db/init.sql");

    static {
        DB.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DB::getJdbcUrl);
        registry.add("spring.datasource.username", DB::getUsername);
        registry.add("spring.datasource.password", DB::getPassword);
    }

    // sin @AfterAll aquí: el contenedor es compartido por todas las clases de
    // prueba y JUnit ejecutaría los métodos heredados tras cada subclase,
    // matando la BD para las siguientes. Ryuk limpia al terminar la JVM.

    @Value("${local.server.port}")
    protected int port;

    protected RestTemplate rest;

    @BeforeEach
    void initRestClient() {
        var factory = new DefaultUriBuilderFactory("http://localhost:" + port);
        rest = new RestTemplate();
        rest.setUriTemplateHandler(factory);
        // no lanzar excepciones en 4xx/5xx: los códigos HTTP son parte de las pruebas
        rest.setErrorHandler(new org.springframework.web.client.ResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    protected ObjectMapper json = new ObjectMapper();

    /** sufijo único para datos compartidos entre clases de test */
    protected static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected HttpHeaders bearer(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders jsonHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Registra un usuario por la API (POST /api/users) y devuelve su JSON.
     */
    protected JsonNode registerUser(String emailPrefix) throws Exception {
        String suffix = suffix();
        var request = json.createObjectNode();
        request.put("name", "User " + suffix);
        request.put("email", emailPrefix + "-" + suffix + "@shop.local");
        request.put("password", "secret-pass-1");
        var response = rest.postForEntity("/api/users",
                new org.springframework.http.HttpEntity<>(request.toString(), jsonHeaders()), String.class);
        return json.readTree(response.getBody());
    }

    /**
     * Login (POST /api/auth/login) y devuelve el accessToken.
     */
    protected String login(String email, String password) throws Exception {
        var request = json.createObjectNode();
        request.put("email", email);
        request.put("password", password);
        var response = rest.postForEntity("/api/auth/login",
                new org.springframework.http.HttpEntity<>(request.toString(), jsonHeaders()), String.class);
        return json.readTree(response.getBody()).path("accessToken").asText();
    }
}
