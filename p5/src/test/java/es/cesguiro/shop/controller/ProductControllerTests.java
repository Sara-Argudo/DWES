package es.cesguiro.shop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.ProductNotFoundException;
import es.cesguiro.shop.controller.domain.model.Money;
import es.cesguiro.shop.controller.domain.model.Product;
import es.cesguiro.shop.controller.domain.model.Stock;
import es.cesguiro.shop.controller.domain.service.ProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import es.cesguiro.shop.persistence.ShopApplication;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de la presentación de productos con el SERVICIO DOBLADO (sin base
 * de datos): cada constraint del contrato responde 400 con campo y motivo en
 * el formato `Problem`; un recurso inexistente responde 404; una correcta,
 * 200 / 201 / 204.
 */
@DisplayName("ProductController (presentación, servicio doblado)")
@WebMvcTest(controllers = ProductController.class)
@ContextConfiguration(classes = ShopApplication.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private String catalog(int id, String name, String description, String price, int stock) {
        return "{\"catalogId\":" + id + ",\"name\":\"" + name
                + "\",\"description\":\"" + description
                + "\",\"price\":" + price + ",\"stock\":" + stock + "}";
    }

    @Nested
    @DisplayName("Constraints del contrato: 400 con campo y motivo")
    class ConstraintsOfTheContract {

        @Test
        @DisplayName("Un precio 0 responde 400 antes de tocar el dominio")
        void zeroPriceIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI", "2 m, 8K", "0", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/validation-failed"))
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("price:")));
        }

        @Test
        @DisplayName("Un precio negativo responde 400")
        void negativePriceIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI", "2 m, 8K", "-1", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("price:")));
        }

        @Test
        @DisplayName("Un precio fuera del máximo del contrato responde 400")
        void tooHighPriceIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI", "2 m, 8K", "1000000", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("price:")));
        }

        @Test
        @DisplayName("Un stock negativo responde 400")
        void negativeStockIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI", "2 m, 8K", "14.90", -1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("stock:")));
        }

        @Test
        @DisplayName("Un stock fuera del máximo del contrato responde 400")
        void tooHighStockIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI", "2 m, 8K", "14.90", 100001)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("stock:")));
        }

        @Test
        @DisplayName("Un catalogId 0 responde 400")
        void zeroCatalogIdIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(0, "Cable HDMI", "2 m, 8K", "14.90", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("catalogId:")));
        }

        @Test
        @DisplayName("Un nombre vacío responde 400")
        void emptyNameIsRejectedWith400() throws Exception {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "", "2 m, 8K", "14.90", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("name:")));
        }

        @Test
        @DisplayName("Un nombre de 81 caracteres responde 400")
        void tooLongNameIsRejectedWith400() throws Exception {
            String name = "x".repeat(81);
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, name, "2 m, 8K", "14.90", 45)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("name:")));
        }

        @Test
        @DisplayName("Una descripción de 241 caracteres responde 400")
        void tooLongDescriptionIsRejectedWith400() throws Exception {
            String description = "y".repeat(241);
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"catalogId\":1,\"name\":\"Cable HDMI\",\"description\":\"" + description
                                    + "\",\"price\":14.90,\"stock\":45}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("description:")));
        }
    }

    @Nested
    @DisplayName("Estado: 404 coherente con el contrato")
    class StateWithProblem {

        private final String validBody = catalog(1, "Cable HDMI 2.1", "2 m, 8K", "14.90", 45);

        @Test
        @DisplayName("GET de un producto inexistente responde 404 con Problem")
        void unknownProductIsNotFound() throws Exception {
            when(productService.getById(999L)).thenThrow(new ProductNotFoundException(999L));

            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/product-not-found"))
                    .andExpect(jsonPath("$.title").value("Producto no existe"));
        }

        @Test
        @DisplayName("PUT de un producto inexistente responde 404 con Problem")
        void updateOfUnknownProductIsNotFound() throws Exception {
            when(productService.update(eq(999L), any(ProductService.ProductInput.class)))
                    .thenThrow(new ProductNotFoundException(999L));

            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("POST cuyo catalogId apunta a un catálogo inexistente responde 404")
        void creationWithUnknownCatalogIsNotFound() throws Exception {
            when(productService.create(any(ProductService.ProductInput.class)))
                    .thenThrow(new CatalogNotFoundException(99L));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/catalog-not-found"));
        }

        @Test
        @DisplayName("DELETE de un producto inexistente responde 404 con Problem")
        void deleteOfUnknownProductIsNotFound() throws Exception {
            doThrow(new ProductNotFoundException(999L)).when(productService).delete(999L);

            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value(Matchers.containsString("999")));
        }
    }

    @Nested
    @DisplayName("Peticiones correctas: 200, 201, 204")
    class CorrectRequests {

        @Test
        @DisplayName("POST válido crea el producto y responde 201 con el modelo del contrato")
        void validCreationReturns201() throws Exception {
            when(productService.create(any(ProductService.ProductInput.class)))
                    .thenReturn(new Product(29L, 1L, "Cable HDMI 2.1", "2 m, 8K",
                            Money.of("14.90"), Stock.of(45)));

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI 2.1", "2 m, 8K", "14.90", 45)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(29))
                    .andExpect(jsonPath("$.catalogId").value(1))
                    .andExpect(jsonPath("$.name").value("Cable HDMI 2.1"))
                    .andExpect(jsonPath("$.price").value(14.90))
                    .andExpect(jsonPath("$.stock").value(45));
        }

        @Test
        @DisplayName("PUT válido modifica el producto y responde 200")
        void validUpdateReturns200() throws Exception {
            when(productService.update(eq(29L), any(ProductService.ProductInput.class)))
                    .thenReturn(new Product(29L, 1L, "Cable HDMI 2.1", "2 m, 4K",
                            Money.of("15.90"), Stock.of(40)));

            mockMvc.perform(put("/api/products/29")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(catalog(1, "Cable HDMI 2.1", "2 m, 4K", "15.90", 40)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(29))
                    .andExpect(jsonPath("$.price").value(15.90));
        }

        @Test
        @DisplayName("DELETE de un producto existente responde 204")
        void validDeleteReturns204() throws Exception {
            mockMvc.perform(delete("/api/products/29"))
                    .andExpect(status().isNoContent());

            verify(productService).delete(29L);
        }
    }
}
