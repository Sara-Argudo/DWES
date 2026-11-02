package es.cesguiro.shop.catalog.controller;

import es.cesguiro.shop.catalog.application.ProductService;
import es.cesguiro.shop.catalog.controller.dto.CatalogDtos.PageDto;
import es.cesguiro.shop.catalog.controller.dto.CatalogDtos.ProductDto;
import es.cesguiro.shop.catalog.controller.dto.CatalogDtos.ProductRequest;
import es.cesguiro.shop.catalog.domain.Product;
import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.Stock;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Productos: lectura pública (por id y paginado por catálogo con page/size);
 * escritura solo ADMIN. Ya sin GET /api/products, sustituido por el listado paginado.
 */
@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products/{id}")
    public ProductDto getById(@PathVariable long id) {
        return ProductDto.from(productService.getById(id));
    }

    @GetMapping("/api/catalogs/{catalogId}/products")
    public PageDto<ProductDto> getByCatalog(@PathVariable long catalogId,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size) {
        Page<Product> products = productService.getByCatalogPage(catalogId, page, size);
        return PageDto.from(products, ProductDto::from);
    }

    @PostMapping("/api/products")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductRequest request,
                                             UriComponentsBuilder uri) {
        Product product = productService.create(
                request.catalogId(),
                request.name(),
                request.description(),
                Money.of(request.price()),
                Stock.of(request.stock())
        );
        var location = uri.path("/api/products/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(location).body(ProductDto.from(product));
    }

    @PutMapping("/api/products/{id}")
    public ProductDto update(@PathVariable long id, @Valid @RequestBody ProductRequest request) {
        return ProductDto.from(productService.update(
                id, request.name(), request.description(),
                Money.of(request.price()), Stock.of(request.stock())
        ));
    }

    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
