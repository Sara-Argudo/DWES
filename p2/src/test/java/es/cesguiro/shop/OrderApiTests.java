package es.cesguiro.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Pedidos: creación con líneas, total, reserva de stock atómica,
 * cancelación con liberación y doble cancelación rechazada,
 * pertenencia (USER ve lo suyo, ADMIN ve todo).
 */
class OrderApiTests extends IntegrationTest {

    private ResponseEntity<String> adminPost(String path, JsonNode body, String adminToken) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body.toString(), bearer(adminToken)), String.class);
    }

    private ResponseEntity<String> postOrder(String token, JsonNode body) {
        return rest.exchange("/api/orders", HttpMethod.POST, new HttpEntity<>(body.toString(), bearer(token)), String.class);
    }

    private int stockOf(long productId) throws Exception {
        return json.readTree(rest.getForEntity("/api/products/" + productId, String.class).getBody())
                .get("stock").asInt();
    }

    @Test
    void orderCapturesUnitPricesComputesTotalAndReservesStock() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        var user = registerUser("buyer");
        String userToken = login(user.get("email").asText(), "secret-pass-1");
        String suffix = suffix();

        // dos productos propios con stock conocido
        for (int i = 0; i < 2; i++) {
            var product = json.createObjectNode();
            product.put("catalogId", 3);
            product.put("name", "Order-" + suffix + "-" + i);
            product.put("description", "test product");
            product.put("price", 10.00 + i);
            product.put("stock", 10);
            adminPost("/api/products", product, adminToken);
        }
        var page = json.readTree(rest.getForEntity(
                "/api/catalogs/3/products?page=0&size=50", String.class).getBody());
        JsonNode content = page.get("content");
        long firstId = content.get(content.size() - 2).get("id").asLong();
        long secondId = content.get(content.size() - 1).get("id").asLong();

        int stockBefore = stockOf(firstId) + stockOf(secondId);

        // pedido con dos líneas: 2 x (10.00) + 1 x (11.00) = 31.00
        var lineOne = json.createObjectNode();
        lineOne.put("productId", firstId);
        lineOne.put("quantity", 2);
        var lineTwo = json.createObjectNode();
        lineTwo.put("productId", secondId);
        lineTwo.put("quantity", 1);
        var lines = json.createArrayNode();
        lines.add(lineOne);
        lines.add(lineTwo);
        var orderBody = json.createObjectNode();
        orderBody.set("lines", lines);

        var created = postOrder(userToken, orderBody);
        assertThat(created.getStatusCode().value()).isEqualTo(201);

        var order = json.readTree(created.getBody());
        assertThat(order.get("state").asText()).isEqualTo("CREATED");
        assertThat(order.get("total").asDouble()).isEqualTo(31.00);
        // necesita userid del token, no del cuerpo
        assertThat(order.get("userId").asLong()).isEqualTo(user.get("id").asLong());

        // el precio capturado por línea es el precio vigente
        assertThat(order.get("lines").get(0).get("unitPrice").asDouble()).isEqualTo(10.00);
        assertThat(order.get("lines").get(1).get("unitPrice").asDouble()).isEqualTo(11.00);

        // el stock de ambos productos se reservó
        assertThat(stockOf(firstId) + stockOf(secondId)).isEqualTo(stockBefore - 3);

        // consulta del pedido por su dueño
        var byOwner = rest.exchange("/api/orders/" + order.get("id").asLong(),
                HttpMethod.GET, new HttpEntity<>(null, bearer(userToken)), String.class);
        assertThat(byOwner.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void failedReservationRollsBackEverything() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        var user = registerUser("atomic");
        String userToken = login(user.get("email").asText(), "secret-pass-1");
        String suffix = suffix();

        // producto 1 con stock alto; producto 2 sin stock
        var big = json.createObjectNode();
        big.put("catalogId", 2);
        big.put("name", "Big-" + suffix);
        big.put("description", "stock 50");
        big.put("price", 5.00);
        big.put("stock", 50);
        long bigId = json.readTree(adminPost("/api/products", big, adminToken).getBody()).get("id").asLong();

        var empty = json.createObjectNode();
        empty.put("catalogId", 2);
        empty.put("name", "Empty-" + suffix);
        empty.put("description", "stock 0");
        empty.put("price", 3.00);
        empty.put("stock", 0);
        long emptyId = json.readTree(adminPost("/api/products", empty, adminToken).getBody()).get("id").asLong();

        var lineOne = json.createObjectNode();
        lineOne.put("productId", bigId);
        lineOne.put("quantity", 5);
        var lineTwo = json.createObjectNode();
        lineTwo.put("productId", emptyId);
        lineTwo.put("quantity", 1);
        var lines = json.createArrayNode();
        lines.add(lineOne);
        lines.add(lineTwo);
        var orderBody = json.createObjectNode();
        orderBody.set("lines", lines);

        var rejected = postOrder(userToken, orderBody);
        assertThat(rejected.getStatusCode().value()).isEqualTo(409);

        // el stock del producto 1 NO quedó reservado (rollback completo)
        assertThat(stockOf(bigId)).isEqualTo(50);
        assertThat(stockOf(emptyId)).isEqualTo(0);
    }

    @Test
    void cancelReleasesStockAndSecondCancelIsRejected() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        var user = registerUser("canceller");
        String userToken = login(user.get("email").asText(), "secret-pass-1");
        String suffix = suffix();

        var product = json.createObjectNode();
        product.put("catalogId", 1);
        product.put("name", "Cancelable-" + suffix);
        product.put("description", "stock 20");
        product.put("price", 4.50);
        product.put("stock", 20);
        long productId = json.readTree(adminPost("/api/products", product, adminToken).getBody()).get("id").asLong();

        var line = json.createObjectNode();
        line.put("productId", productId);
        line.put("quantity", 4);
        var lines = json.createArrayNode();
        lines.add(line);
        var orderBody = json.createObjectNode();
        orderBody.set("lines", lines);

        long orderId = json.readTree(postOrder(userToken, orderBody).getBody()).get("id").asLong();
        assertThat(stockOf(productId)).isEqualTo(16);

        // cancelar: libera el stock
        var cancelled = rest.exchange("/api/orders/" + orderId + "/cancel", HttpMethod.POST,
                new HttpEntity<>(null, bearer(userToken)), String.class);
        assertThat(cancelled.getStatusCode().value()).isEqualTo(200);
        assertThat(json.readTree(cancelled.getBody()).get("state").asText()).isEqualTo("CANCELLED");
        assertThat(stockOf(productId)).isEqualTo(20);

        // segunda cancelación: 409
        var second = rest.exchange("/api/orders/" + orderId + "/cancel", HttpMethod.POST,
                new HttpEntity<>(null, bearer(userToken)), String.class);
        assertThat(second.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void otherUsersCannotSeeOrdersButAdminCan() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        var owner = registerUser("owner");
        String ownerToken = login(owner.get("email").asText(), "secret-pass-1");
        var stranger = registerUser("stranger");
        String strangerToken = login(stranger.get("email").asText(), "secret-pass-1");

        var line = json.createObjectNode();
        line.put("productId", 21);
        line.put("quantity", 1);
        var lines = json.createArrayNode();
        lines.add(line);
        var orderBody = json.createObjectNode();
        orderBody.set("lines", lines);

        long orderId = json.readTree(postOrder(ownerToken, orderBody).getBody()).get("id").asLong();

        // extraño: 403
        var denied = rest.exchange("/api/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(null, bearer(strangerToken)), String.class);
        assertThat(denied.getStatusCode().value()).isEqualTo(403);

        // dueño: 200 y /api/me/orders lo lista
        var mine = rest.exchange("/api/me/orders", HttpMethod.GET,
                new HttpEntity<>(null, bearer(ownerToken)), String.class);
        assertThat(mine.getStatusCode().value()).isEqualTo(200);
        assertThat(json.readTree(mine.getBody()).size()).isEqualTo(1);

        // admin ve el pedido de otro
        var adminView = rest.exchange("/api/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(null, bearer(adminToken)), String.class);
        assertThat(adminView.getStatusCode().value()).isEqualTo(200);

        // sin token: 401
        var anonymous = rest.getForEntity("/api/orders/" + orderId, String.class);
        assertThat(anonymous.getStatusCode().value()).isEqualTo(401);
    }
}
