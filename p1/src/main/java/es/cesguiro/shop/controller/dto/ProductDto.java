package es.cesguiro.shop.controller.dto;

import java.math.BigDecimal;

import es.cesguiro.shop.domain.Product;

/**
 * DTO de transporte de la capa web: lo que la API expone al exterior.
 * No es el modelo de dominio; lo mapea el factory method from(Product).
 */
public record ProductDto(
        Long id,
        Long catalogId,
        String name,
        String description,
        BigDecimal price,
        int stock
) {

    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getCatalogId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock()
        );
    }
}
