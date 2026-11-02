package es.cesguiro.shop.controller;

import java.util.List;

import es.cesguiro.shop.domain.Product;
import es.cesguiro.shop.domain.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Único endpoint de la partida: GET /api/products.
 * Devuelve el listado completo de productos en JSON.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }
}
