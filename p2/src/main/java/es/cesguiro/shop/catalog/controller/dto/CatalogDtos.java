package es.cesguiro.shop.catalog.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import es.cesguiro.shop.catalog.domain.Catalog;
import es.cesguiro.shop.catalog.domain.Product;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * DTOs de transporte de la API de catálogos y productos (frontera web).
 */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CatalogDto(long id, String name, String description) {
        public static CatalogDto from(Catalog catalog) {
            return new CatalogDto(catalog.getId(), catalog.getName(), catalog.getDescription());
        }
    }

    public record CatalogRequest(
            @NotBlank(message = "name is required") @Size(max = 80) String name,
            @NotBlank(message = "description is required") @Size(max = 240) String description
    ) {
    }

    public record ProductDto(
            long id,
            long catalogId,
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
                    product.getPrice().amount(),
                    product.getStock().units()
            );
        }
    }

    public record ProductRequest(
            @NotNull(message = "catalogId is required") Long catalogId,
            @NotBlank(message = "name is required") @Size(max = 80) String name,
            @NotBlank(message = "description is required") @Size(max = 240) String description,
            @NotNull(message = "price is required")
            @Positive(message = "price must be greater than zero")
            @Digits(integer = 6, fraction = 2, message = "price must have at most two decimals") BigDecimal price,
            @NotNull(message = "stock is required")
            @PositiveOrZero(message = "stock cannot be negative") Integer stock
    ) {
    }

    public record PageDto<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static <T, R> PageDto<R> from(Page<T> page, Function<T, R> mapper) {
            List<R> content = page.getContent().stream().map(mapper).toList();
            return new PageDto<>(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        }
    }
}
