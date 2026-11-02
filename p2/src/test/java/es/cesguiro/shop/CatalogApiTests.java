package es.cesguiro.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Catálogos y productos: CRUD público de lectura, ADMIN para escritura,
 * 409 por nombre de catálogo repetido y catálogo con productos,
 * 404 por catálogo inexistente, precio/stock con sus reglas y paginación.
 */
class CatalogApiTests extends IntegrationTest {

    private ResponseEntity<String> adminPost(String path, JsonNode body, String adminToken) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body.toString(), bearer(adminToken)), String.class);
    }

    @Test
    void adminCreatesAndReadsCatalogAndProduct() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        String suffix = suffix();

        // crea catálogo
        var catalog = json.createObjectNode();
        catalog.put("name", "Catalogos-" + suffix);
        catalog.put("description", "Creation and list");
        var catalogResponse = adminPost("/api/catalogs", catalog, adminToken);
        assertThat(catalogResponse.getStatusCode().value()).isEqualTo(201);
        long catalogId = json.readTree(catalogResponse.getBody()).get("id").asLong();

        // lectura pública
        var publicGet = rest.getForEntity("/api/catalogs/" + catalogId, String.class);
        assertThat(publicGet.getStatusCode().value()).isEqualTo(200);

        // crea producto en el catálogo
        var product = json.createObjectNode();
        product.put("catalogId", catalogId);
        product.put("name", "Product-" + suffix);
        product.put("description", "A product");
        product.put("price", 29.99);
        product.put("stock", 5);
        var productResponse = adminPost("/api/products", product, adminToken);
        assertThat(productResponse.getStatusCode().value()).isEqualTo(201);
        long productId = json.readTree(productResponse.getBody()).get("id").asLong();

        var productGet = rest.getForEntity("/api/products/" + productId, String.class);
        assertThat(productGet.getStatusCode().value()).isEqualTo(200);
        var created = json.readTree(productGet.getBody());
        assertThat(created.get("catalogId").asLong()).isEqualTo(catalogId);
        assertThat(created.get("price").asDouble()).isEqualTo(29.99);
        assertThat(created.get("stock").asInt()).isEqualTo(5);
    }

    @Test
    void duplicateCatalogNameIsRejectedWith409() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        String suffix = suffix();

        var catalog = json.createObjectNode();
        catalog.put("name", "Duplicate-" + suffix);
        catalog.put("description", "First");
        var first = adminPost("/api/catalogs", catalog, adminToken);
        assertThat(first.getStatusCode().value()).isEqualTo(201);

        var second = adminPost("/api/catalogs", catalog, adminToken);
        assertThat(second.getStatusCode().value()).isEqualTo(409);
        var problem = json.readTree(second.getBody());
        assertThat(problem.get("title").asText()).isEqualTo("Conflict");
        assertThat(problem.get("status").asInt()).isEqualTo(409);
    }

    @Test
    void catalogWithProductsCannotBeDeleted() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        String suffix = suffix();

        var catalog = json.createObjectNode();
        catalog.put("name", "NotEmpty-" + suffix);
        catalog.put("description", "Will contain products");
        long catalogId = json.readTree(adminPost("/api/catalogs", catalog, adminToken).getBody()).get("id").asLong();

        var product = json.createObjectNode();
        product.put("catalogId", catalogId);
        product.put("name", "Stops-Deletion-" + suffix);
        product.put("description", "blocking deletion");
        product.put("price", 9.99);
        product.put("stock", 3);
        adminPost("/api/products", product, adminToken);

        var forbidden = rest.exchange("/api/catalogs/" + catalogId, HttpMethod.DELETE,
                new HttpEntity<>(null, bearer(adminToken)), String.class);
        assertThat(forbidden.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void productInUnknownCatalogIsRejectedWith404() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        var product = json.createObjectNode();
        product.put("catalogId", 999_999);
        product.put("name", "Orphan-" + suffix());
        product.put("description", "no catalog");
        product.put("price", 5.50);
        product.put("stock", 1);

        var response = adminPost("/api/products", product, adminToken);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(json.readTree(response.getBody()).get("title").asText()).isEqualTo("Not Found");
    }

    @Test
    void invalidPriceAndNegativeStockAreRejectedWith400() throws Exception {
        String adminToken = login("admin@shop.local", "admin-pass-123");
        String suffix = suffix();

        var zeroPrice = json.createObjectNode();
        zeroPrice.put("catalogId", 1);
        zeroPrice.put("name", "Zero-" + suffix);
        zeroPrice.put("description", "bad price");
        zeroPrice.put("price", 0);
        zeroPrice.put("stock", 1);
        assertThat(adminPost("/api/products", zeroPrice, adminToken).getStatusCode().value()).isEqualTo(400);

        var threeDecimals = json.createObjectNode();
        threeDecimals.put("catalogId", 1);
        threeDecimals.put("name", "Decimals-" + suffix);
        threeDecimals.put("description", "bad precision");
        threeDecimals.put("price", 9.999);
        threeDecimals.put("stock", 1);
        assertThat(adminPost("/api/products", threeDecimals, adminToken).getStatusCode().value()).isEqualTo(400);

        var negativeStock = json.createObjectNode();
        negativeStock.put("catalogId", 1);
        negativeStock.put("name", "NegStock-" + suffix);
        negativeStock.put("description", "negative stock");
        negativeStock.put("price", 9.99);
        negativeStock.put("stock", -1);
        assertThat(adminPost("/api/products", negativeStock, adminToken).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void productsByCatalogArePaginated() throws Exception {
        String suffix = suffix();

        // catálogo 1 de la semilla tiene 12 productos; página 0 con 5
        var first = json.readTree(rest.getForEntity("/api/catalogs/1/products?page=0&size=5", String.class).getBody());
        assertThat(first.get("totalElements").asLong()).isEqualTo(12);
        assertThat(first.get("totalPages").asInt()).isEqualTo(3);
        assertThat(first.get("content").size()).isEqualTo(5);

        // página 1
        var second = json.readTree(rest.getForEntity("/api/catalogs/1/products?page=1&size=5", String.class).getBody());
        assertThat(second.get("content").size()).isEqualTo(5);
        assertThat(second.get("page").asInt()).isEqualTo(1);

        // catálogo inexistente -> 404
        var unknown = rest.getForEntity("/api/catalogs/999999/products?page=0&size=5", String.class);
        assertThat(unknown.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void writesWithoutAdminTokenAreForbiddenOrUnauthorized() throws Exception {
        // sin token: 401
        var body = json.createObjectNode();
        body.put("name", "Anon-" + suffix());
        body.put("description", "no token");
        var anonymous = rest.postForEntity("/api/catalogs",
                new org.springframework.http.HttpEntity<>(body.toString(), jsonHeaders()), String.class);
        assertThat(anonymous.getStatusCode().value()).isEqualTo(401);

        // con token USER (no ADMIN): 403
        var user = registerUser("writer");
        String userToken = login(user.get("email").asText(), "secret-pass-1");
        var denied = rest.exchange("/api/catalogs", HttpMethod.POST,
                new HttpEntity<>(body, bearer(userToken)), String.class);
        assertThat(denied.getStatusCode().value()).isEqualTo(403);

        // listado público sigue libre
        var list = rest.getForEntity("/api/catalogs", String.class);
        assertThat(list.getStatusCode().value()).isEqualTo(200);
    }
}
