package es.cesguiro.shop.controller.domain.service;

import java.math.BigDecimal;
import java.util.List;

import es.cesguiro.shop.controller.domain.model.Product;

/**
 * Interfaz del servicio de productos del dominio.
 * Para el alta y la modificación recibe los datos CRUDOS de un producto
 * (record `ProductInput`, sin `id`): el servicio construye primero los value
 * objects `Money` y `Stock` y completa el 404 de las FK, de modo que un
 * estado imposible nunca llega a la persistencia.
 */
public interface ProductService {

    /** Datos crudos de un producto para su alta o modificación (sin `id`, con `catalogId`). */
    record ProductInput(Long catalogId, String name, String description, BigDecimal price, Integer stock) {
    }

    Product getById(Long id);

    Product create(ProductInput input);

    Product update(Long id, ProductInput input);

    void delete(Long id);

    /** Productos de un catálogo, en la página `page` con `size` productos por página. */
    List<Product> getByCatalogId(Long catalogId, int page, int size);

    long countByCatalogId(Long catalogId);
}
