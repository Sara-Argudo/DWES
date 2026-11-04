package es.cesguiro.shop.controller.domain.repository;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.controller.domain.model.Catalog;

/**
 * Interfaz del repositorio de catálogos que el dominio necesita.
 * La implementación vive en la capa de persistencia del módulo (inversión de
 * dependencias); aquí no se menciona JDBC ni ninguna tecnología de acceso a datos.
 */
public interface CatalogRepository {

    List<Catalog> getAll();

    Optional<Catalog> getById(Long id);

    Optional<Catalog> findByName(String name);

    /** Inserta el catálogo y devuelve el modelo con el `id` generado. */
    Catalog save(Catalog catalog);

    void update(Catalog catalog);

    void deleteById(Long id);
}
