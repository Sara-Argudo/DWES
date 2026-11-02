package es.cesguiro.shop.catalog.api;

import es.cesguiro.shop.catalog.application.ProductService;
import es.cesguiro.shop.catalog.exception.InsufficientStockException;
import es.cesguiro.shop.catalog.infrastructure.persistence.ProductDataJpa;
import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.Stock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del contrato CatalogApi dentro del módulo catalog.
 * reserve/release usan UPDATE atómicos con comprobación de stock;
 * el rollback de la transacción del pedido deja todo como estaba si falla.
 */
@Service
public class CatalogApiImpl implements CatalogApi {

    private final ProductDataJpa products;
    private final ProductService productService;

    public CatalogApiImpl(ProductDataJpa products, ProductService productService) {
        this.products = products;
        this.productService = productService;
    }

    @Override
    @Transactional(readOnly = true)
    public Money priceOf(long productId) {
        return productService.getById(productId).getPrice();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void reserve(long productId, int units) {
        int reserved = products.reserveStock(productId, Stock.of(units));
        if (reserved == 0) {
            throw new InsufficientStockException(productId, units);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void release(long productId, int units) {
        products.releaseStock(productId, Stock.of(units));
    }
}
