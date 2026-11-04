package es.cesguiro.shop.controller;

import es.cesguiro.shop.api.ProductsApi;
import es.cesguiro.shop.controller.domain.service.ProductService;
import es.cesguiro.shop.controller.mapper.ProductMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de productos: implementa la interfaz ProductsApi generada
 * a partir del contrato openapi.yaml. Delega en el servicio del dominio y
 * traduce entre los DTO del contrato y los modelos del dominio con el mapper.
 *
 * Nota: el contrato y el dominio usan ambos una clase llamada `Product`;
 * aquí se hace referencia al modelo del dominio con el nombre completo del
 * paquete.
 */
@RestController
public class ProductController implements ProductsApi {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Product> createProduct(
            es.cesguiro.shop.controller.dto.ProductRequest productRequest) {
        es.cesguiro.shop.controller.dto.Product created = ProductMapper.toApi(
                productService.create(toInput(productRequest)));
        return ResponseEntity.status(201).body(created);
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Product> getProduct(Long id) {
        return ResponseEntity.ok(ProductMapper.toApi(productService.getById(id)));
    }

    @Override
    public ResponseEntity<es.cesguiro.shop.controller.dto.Product> updateProduct(
            Long id, es.cesguiro.shop.controller.dto.ProductRequest productRequest) {
        es.cesguiro.shop.controller.dto.Product updated = ProductMapper.toApi(
                productService.update(id, toInput(productRequest)));
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Recorta los datos del DTO: el servicio construye los value objects. */
    private ProductService.ProductInput toInput(es.cesguiro.shop.controller.dto.ProductRequest request) {
        return new ProductService.ProductInput(
                request.getCatalogId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStock()
        );
    }
}
