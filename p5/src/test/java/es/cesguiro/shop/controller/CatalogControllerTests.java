package es.cesguiro.shop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.cesguiro.shop.controller.domain.exception.CatalogNotEmptyException;
import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.service.CatalogService;
import es.cesguiro.shop.controller.domain.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import es.cesguiro.shop.persistence.ShopApplication;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de la presentación con el SERVICIO DOBLADO (sin base de datos):
 * una petición que no cumple cada constraint del contrato responde 400 con
 * campo y motivo en el formato `Problem`; un recurso inexistente responde 404
 * con el problema coherente; una correcta, 200 / 201 / 204.
 */
@DisplayName("CatalogController (presentación, servicio doblado)")
@WebMvcTest(controllers = CatalogController.class)
@ContextConfiguration(classes = ShopApplication.class)
class CatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @MockitoBean
    private ProductService productService;

    // ---- constraints del contrato: 400 con campo y motivo -----------------

    @Test
    @DisplayName("POST con nombre vacío responde 400 con el campo y el motivo")
    void emptyNameIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Entrada y salida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/validation-failed"))
                .andExpect(jsonPath("$.title").value("Petición no válida"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("name:")));
    }

    @Test
    @DisplayName("POST con nombre de 81 caracteres responde 400 por el longitudes")
    void tooLongNameIsRejectedWith400() throws Exception {
        String name = "x".repeat(81);
        mockMvc.perform(post("/api/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"Entrada y salida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("name:")));
    }

    @Test
    @DisplayName("POST con descripción de 241 caracteres responde 400")
    void tooLongDescriptionIsRejectedWith400() throws Exception {
        String description = "y".repeat(241);
        mockMvc.perform(post("/api/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novedades\",\"description\":\"" + description + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("description:")));
    }

    // ---- estado: 404 y 409 con el problema coherente -----------------------

    @Test
    @DisplayName("GET de un catálogo inexistente responde 404 con Problem")
    void unknownCatalogIsNotFound() throws Exception {
        when(catalogService.getById(999L)).thenThrow(new CatalogNotFoundException(999L));

        mockMvc.perform(get("/api/catalogs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/catalog-not-found"))
                .andExpect(jsonPath("$.title").value("Catálogo no existe"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("999")));
    }

    @Test
    @DisplayName("DELETE de un catálogo con productos responde 409 con Problem")
    void nonEmptyCatalogIsConflict() throws Exception {
        when(catalogService.getById(1L)).thenReturn(new Catalog(1L, "Periféricos", "y"));
        doThrow(new CatalogNotEmptyException(1L, 12L)).when(catalogService).delete(1L);

        mockMvc.perform(delete("/api/catalogs/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("https://cesguiro.es/shop/errors/catalog-not-empty"));
    }

    // ---- peticiones correctas: 200, 201, 204 -------------------------------

    @Test
    @DisplayName("POST válido crea el catálogo y responde 201")
    void validCreationReturns201() throws Exception {
        when(catalogService.create(any(Catalog.class)))
                .thenReturn(new Catalog(4L, "Novedades", "Lo último"));

        mockMvc.perform(post("/api/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novedades\",\"description\":\"Lo último\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Novedades"))
                .andExpect(jsonPath("$.description").value("Lo último"));
    }

    @Test
    @DisplayName("PUT válido modifica el catálogo y responde 200")
    void validUpdateReturns200() throws Exception {
        when(catalogService.update(org.mockito.ArgumentMatchers.eq(4L), any(Catalog.class)))
                .thenReturn(new Catalog(4L, "Novedades", "Actualizado"));

        mockMvc.perform(put("/api/catalogs/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novedades\",\"description\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.description").value("Actualizado"));
    }

    @Test
    @DisplayName("DELETE de un catálogo vacío responde 204")
    void validDeleteReturns204() throws Exception {
        when(catalogService.getById(3L)).thenReturn(new Catalog(3L, "Accesorios", "y"));

        mockMvc.perform(delete("/api/catalogs/3"))
                .andExpect(status().isNoContent());

        verify(catalogService).delete(3L);
    }

    @Test
    @DisplayName("GET /api/catalogs lista los catálogos")
    void listsCatalogs() throws Exception {
        when(catalogService.getAll())
                .thenReturn(java.util.List.of(new Catalog(1L, "Periféricos", "y")));

        mockMvc.perform(get("/api/catalogs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Periféricos"));
    }
}
