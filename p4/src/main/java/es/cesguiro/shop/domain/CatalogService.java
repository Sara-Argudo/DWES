package es.cesguiro.shop.domain;

import java.util.List;

/**
 * Interfaz del servicio de catálogos del dominio.
 */
public interface CatalogService {

    List<Catalog> getAll();

    Catalog getById(Long id);

    Catalog create(Catalog catalog);

    Catalog update(Long id, Catalog catalog);

    void delete(Long id);
}
