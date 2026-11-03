package es.cesguiro.shop.domain;

import java.util.List;

/**
 * Interfaz del servicio de productos del dominio.
 */
public interface ProductService {

    Product getById(Long id);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);

    /** Productos de un catálogo, en la página `page` con `size` productos por página. */
    List<Product> getByCatalogId(Long catalogId, int page, int size);

    long countByCatalogId(Long catalogId);
}
