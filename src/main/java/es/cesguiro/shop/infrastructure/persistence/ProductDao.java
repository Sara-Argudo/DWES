package es.cesguiro.shop.infrastructure.persistence;

import java.util.List;

/**
 * DAO de productos: consulta la base de datos y trabaja con su entidad.
 */
public interface ProductDao {

    List<ProductEntity> getAll();
}
