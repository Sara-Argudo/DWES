package es.cesguiro.shop.controller.domain.persistence;

import java.util.List;
import java.util.Optional;

/**
 * DAO de catálogos: consulta la base de datos y trabaja con su entidad.
 */
public interface CatalogDao {

    List<CatalogEntity> getAll();

    Optional<CatalogEntity> getById(Long id);

    Optional<CatalogEntity> findByName(String name);

    /** Inserta el catálogo y devuelve el id generado. */
    Long save(CatalogEntity catalog);

    void update(CatalogEntity catalog);

    void deleteById(Long id);
}
