package es.cesguiro.shop.controller;

import java.util.List;

import es.cesguiro.shop.api.CatalogsApi;
import es.cesguiro.shop.api.model.Catalog;
import es.cesguiro.shop.api.model.CatalogRequest;
import es.cesguiro.shop.api.model.Product;
import es.cesguiro.shop.api.model.ProductPage;
import es.cesguiro.shop.domain.CatalogService;
import es.cesguiro.shop.domain.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de catálogos: implementa la interfaz CatalogsApi generada
 * a partir del contrato openapi.yaml. Delega en el servicio del dominio y
 * traduce entre los modelos del contrato (API) y los del dominio.
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
    public ResponseEntity<List<Catalog>> getCatalogs() {
        List<Catalog> catalogs = catalogService.getAll().stream()
                .map(this::toApi)
                .toList();
        return ResponseEntity.ok(catalogs);
    }

    @Override
    public ResponseEntity<Catalog> createCatalog(CatalogRequest catalogRequest) {
        Catalog created = toApi(catalogService.create(toDomain(catalogRequest)));
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Catalog> getCatalog(Long id) {
        return ResponseEntity.ok(toApi(catalogService.getById(id)));
    }

    @Override
    public ResponseEntity<Catalog> updateCatalog(Long id, CatalogRequest catalogRequest) {
        Catalog updated = toApi(catalogService.update(id, toDomain(catalogRequest)));
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> deleteCatalog(Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductPage> getCatalogProducts(Long id, Integer page, Integer size) {
        // 404 si el catálogo no existe; después, la página de productos.
        catalogService.getById(id);

        long totalElements = productService.countByCatalogId(id);
        List<Product> content = productService.getByCatalogId(id, page, size).stream()
                .map(product -> new Product(
                        product.getId(),
                        product.getCatalogId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock()
                ))
                .toList();

        ProductPage productPage = new ProductPage(
                content,
                page,
                size,
                totalElements,
                totalPages(totalElements, size)
        );
        return ResponseEntity.ok(productPage);
    }

    private int totalPages(long totalElements, int size) {
        if (size <= 0) {
            return 0;
        }
        return (int) ((totalElements + size - 1) / size);
    }

    private Catalog toApi(es.cesguiro.shop.domain.Catalog catalog) {
        return new Catalog(
                catalog.getId(),
                catalog.getName(),
                catalog.getDescription()
        );
    }

    private es.cesguiro.shop.domain.Catalog toDomain(CatalogRequest request) {
        return new es.cesguiro.shop.domain.Catalog(
                null,
                request.getName(),
                request.getDescription()
        );
    }
}
