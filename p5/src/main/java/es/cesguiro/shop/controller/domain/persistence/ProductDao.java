package es.cesguiro.shop.controller.domain.persistence;

import java.util.List;
import java.util.Optional;

/**
 * DAO de productos: consulta la base de datos y trabaja con su entidad.
 */
public interface ProductDao {

    Optional<ProductEntity> getById(Long id);

    /** Productos de un catálogo, con corte LIMIT y OFFSET resueltos con JDBC. */
    List<ProductEntity> getPageByCatalogId(Long catalogId, int limit, int offset);

    /** Número total de productos de un catálogo. */
    long countByCatalogId(Long catalogId);

    /** Inserta el producto y devuelve el id generado. */
    Long save(ProductEntity product);

    void update(ProductEntity product);

    void deleteById(Long id);
}
