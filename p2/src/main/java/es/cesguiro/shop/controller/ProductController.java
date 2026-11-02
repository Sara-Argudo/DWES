package es.cesguiro.shop.controller;

import java.util.List;

import es.cesguiro.shop.controller.dto.ProductDto;
import es.cesguiro.shop.domain.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Único endpoint de la partida: GET /api/products.
 * La capa web solo habla su propio idioma: expone DTOs,
 * nunca el modelo de dominio. El mapeo Product → ProductDto
 * hace de frontera junto a la API del servicio.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDto> getAll() {
        return productService.getAll().stream()
                .map(ProductDto::from)
                .toList();
    }
}
