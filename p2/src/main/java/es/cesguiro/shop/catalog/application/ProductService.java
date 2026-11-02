package es.cesguiro.shop.catalog.application;

import es.cesguiro.shop.catalog.domain.Catalog;
import es.cesguiro.shop.catalog.domain.Product;
import es.cesguiro.shop.catalog.exception.CatalogNotFoundException;
import es.cesguiro.shop.catalog.exception.ProductNotFoundException;
import es.cesguiro.shop.catalog.infrastructure.persistence.CatalogDataJpa;
import es.cesguiro.shop.catalog.infrastructure.persistence.ProductDataJpa;
import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.Stock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de los productos: CRUD con catálogo existente (404 si no existe),
 * listado paginado por catálogo y stock controlado con el VO Stock.
 */
@Service
public class ProductService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProductDataJpa products;
    private final CatalogDataJpa catalogs;

    public ProductService(ProductDataJpa products, CatalogDataJpa catalogs) {
        this.products = products;
        this.catalogs = catalogs;
    }

    @Transactional(readOnly = true)
    public Product getById(long id) {
        return products.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Product> getByCatalogPage(long catalogId, Integer page, Integer size) {
        Catalog catalog = catalogs.findById(catalogId)
                .orElseThrow(() -> new CatalogNotFoundException(catalogId));
        int safeSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : size;
        int safePage = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return products.findByCatalogIdOrderByIdAsc(catalog.getId(), pageable);
    }

    @Transactional
    public Product create(long catalogId, String name, String description, Money price, Stock stock) {
        Catalog catalog = catalogs.findById(catalogId)
                .orElseThrow(() -> new CatalogNotFoundException(catalogId));
        return products.save(new Product(catalog.getId(), name, description, price, stock));
    }

    @Transactional
    public Product update(long id, String name, String description, Money price, Stock stock) {
        Product product = getById(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }

    @Transactional
    public void delete(long id) {
        Product product = getById(id);
        products.delete(product);
    }
}
