package es.cesguiro.shop.controller;

import es.cesguiro.shop.api.ProductsApi;
import es.cesguiro.shop.api.model.Product;
import es.cesguiro.shop.api.model.ProductRequest;
import es.cesguiro.shop.domain.CatalogService;
import es.cesguiro.shop.domain.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de productos: implementa la interfaz ProductsApi generada
 * a partir del contrato openapi.yaml. Delega en el servicio del dominio y
 * traduce entre los modelos del contrato (API) y los del dominio.
 *
 * Nota: el contrato y el dominio usan ambos una clase llamada `Product`;
 * aquí se importa la del contrato y el modelo del dominio se menciona con
 * el nombre completo del paquete.
 */
@RestController
public class ProductController implements ProductsApi {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<Product> createProduct(ProductRequest productRequest) {
        Product created = toApi(productService.create(toDomain(productRequest)));
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<Product> getProduct(Long id) {
        return ResponseEntity.ok(toApi(productService.getById(id)));
    }

    @Override
    public ResponseEntity<Product> updateProduct(Long id, ProductRequest productRequest) {
        Product updated = toApi(productService.update(id, toDomain(productRequest)));
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Product toApi(es.cesguiro.shop.domain.Product product) {
        return new Product(
                product.getId(),
                product.getCatalogId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }

    private es.cesguiro.shop.domain.Product toDomain(ProductRequest request) {
        return new es.cesguiro.shop.domain.Product(
                null,
                request.getCatalogId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock()
        );
    }
}
