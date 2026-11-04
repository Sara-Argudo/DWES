package es.cesguiro.shop.controller;

import java.util.List;

import es.cesguiro.shop.controller.domain.service.CatalogService;
import es.cesguiro.shop.controller.domain.service.ProductService;
import es.cesguiro.shop.controller.mapper.CatalogMapper;
import es.cesguiro.shop.controller.mapper.ProductMapper;
import es.cesguiro.shop.api.CatalogsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de catálogos: implementa la interfaz CatalogsApi generada
 * a partir del contrato openapi.yaml. Delega en el servicio del dominio y
 * traduce entre los DTO del contrato y los modelos del dominio con los
 * mappers del módulo.
 */
@RestController
public class CatalogController implements CatalogsApi {

    private final CatalogService catalogService;
    private final ProductService productService;

    public CatalogController(CatalogService catalogService, ProductService productService) {
        this.catalogService = catalogService;
        this.productService = productService;
    }

    @Override
    public ResponseEntity<List<es.cesguiro.shop.controller.dto.Catalog>> getCatalogs() {
        List<es.cesguiro.shop.controller.dto.Catalog> catalogs = catalogService.getAll().stream()
                .map(CatalogMapper::toApi)
                .toList();
        return ResponseEntity.ok(catalogs);
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Catalog> createCatalog(
            es.cesguiro.shop.controller.dto.CatalogRequest catalogRequest) {
        es.cesguiro.shop.controller.dto.Catalog created =
                CatalogMapper.toApi(catalogService.create(CatalogMapper.toDomain(catalogRequest)));
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Catalog> getCatalog(Long id) {
        return ResponseEntity.ok(CatalogMapper.toApi(catalogService.getById(id)));
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Catalog> updateCatalog(
            Long id, es.cesguiro.shop.controller.dto.CatalogRequest catalogRequest) {
        es.cesguiro.shop.controller.dto.Catalog updated =
                CatalogMapper.toApi(catalogService.update(id, CatalogMapper.toDomain(catalogRequest)));
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> deleteCatalog(Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.ProductPage> getCatalogProducts(
            Long id, Integer page, Integer size) {
        // 404 si el catálogo no existe; después, la página de productos.
        catalogService.getById(id);

        long totalElements = productService.countByCatalogId(id);
        es.cesguiro.shop.controller.dto.ProductPage productPage = ProductMapper.toPage(
                productService.getByCatalogId(id, page, size),
                page,
                size,
                totalElements
        );
        return ResponseEntity.ok(productPage);
    }
}
