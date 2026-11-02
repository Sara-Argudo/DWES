package es.cesguiro.shop.catalog.application;

import es.cesguiro.shop.catalog.domain.Catalog;
import es.cesguiro.shop.catalog.exception.CatalogNotEmptyException;
import es.cesguiro.shop.catalog.exception.CatalogNotFoundException;
import es.cesguiro.shop.catalog.exception.DuplicateCatalogNameException;
import es.cesguiro.shop.catalog.infrastructure.persistence.CatalogDataJpa;
import es.cesguiro.shop.catalog.infrastructure.persistence.ProductDataJpa;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del catálogo: crear, consultar, modificar y borrar catálogos.
 * No acepta nombres repetidos ni borra catálogos que tienen productos (409).
 */
@Service
public class CatalogService {

    private final CatalogDataJpa catalogs;
    private final ProductDataJpa products;

    public CatalogService(CatalogDataJpa catalogs, ProductDataJpa products) {
        this.catalogs = catalogs;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<Catalog> getAll() {
        return catalogs.findByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Catalog getById(long id) {
        return catalogs.findById(id)
                .orElseThrow(() -> new CatalogNotFoundException(id));
    }

    @Transactional
    public Catalog create(String name, String description) {
        if (catalogs.existsByNameIgnoreCase(name)) {
            throw new DuplicateCatalogNameException(name);
        }
        return catalogs.save(new Catalog(name, description));
    }

    @Transactional
    public Catalog update(long id, String name, String description) {
        Catalog catalog = getById(id);
        catalogs.findByNameIgnoreCase(name).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateCatalogNameException(name);
            }
        });
        catalog.setName(name);
        catalog.setDescription(description);
        return catalog;
    }

    @Transactional
    public void delete(long id) {
        Catalog catalog = getById(id);
        if (products.existsByCatalogId(id)) {
            throw new CatalogNotEmptyException(id);
        }
        catalogs.delete(catalog);
    }
}
