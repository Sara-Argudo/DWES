package es.cesguiro.shop.domain;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio que el dominio necesita.
 * La implementación vive en la capa de persistencia (inversión de dependencias);
 * aquí no se menciona JDBC ni ninguna tecnología de acceso a datos.
 */
public interface ProductRepository {

    Optional<Product> getById(Long id);

    /** Productos de un catálogo, en la página `page` con `size` productos por página. */
    List<Product> getByCatalogId(Long catalogId, int page, int size);

    long countByCatalogId(Long catalogId);

    /** Inserta el producto y devuelve el modelo con el `id` generado. */
    Product save(Product product);

    void update(Product product);

    void deleteById(Long id);
}
