package es.cesguiro.shop.controller.mapper;

import java.util.List;

import es.cesguiro.shop.controller.domain.model.Product;

/**
 * Mapper del módulo controller: traduce entre el DTO del contrato
 * (`Product`, `ProductRequest` y `ProductPage`, generados en controller.dto)
 * y el modelo del dominio.
 *
 * Nota: el contrato y el dominio usan ambos una clase llamada `Product`; aquí
 * se escribe el nombre completo del paquete del modelo del dominio.
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    public static es.cesguiro.shop.controller.dto.Product toApi(Product product) {
        return new es.cesguiro.shop.controller.dto.Product(
                product.getId(),
                product.getCatalogId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().amount(),
                product.getStock().units()
        );
    }

    /**
     * Página de productos con sus metadatos, según el contrato (`ProductPage`).
     */
    public static es.cesguiro.shop.controller.dto.ProductPage toPage(
            List<Product> products, Integer page, Integer size, long totalElements) {
        List<es.cesguiro.shop.controller.dto.Product> content = products.stream()
                .map(ProductMapper::toApi)
                .toList();
        return new es.cesguiro.shop.controller.dto.ProductPage(
                content,
                page,
                size,
                totalElements,
                totalPages(totalElements, size)
        );
    }

    private static int totalPages(long totalElements, Integer size) {
        if (size == null || size <= 0) {
            return 0;
        }
        return (int) ((totalElements + size - 1) / size);
    }
}
