package es.cesguiro.shop.controller.domain.service;

import java.util.List;

import es.cesguiro.shop.controller.domain.model.Catalog;

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
