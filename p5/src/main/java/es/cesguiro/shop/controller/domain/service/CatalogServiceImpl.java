package es.cesguiro.shop.controller.domain.service;

import java.util.List;

import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.exception.CatalogNotEmptyException;
import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.DuplicateCatalogNameException;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import es.cesguiro.shop.controller.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de catálogos.
 * Depende de las interfaces de los repositorios (definidas en domain), no de
 * sus implementaciones, y aplica las reglas de estado que declara el contrato:
 * nombre repetido (409) y catálogo con productos que no se puede borrar (409).
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final ProductRepository productRepository;

    public CatalogServiceImpl(CatalogRepository catalogRepository, ProductRepository productRepository) {
        this.catalogRepository = catalogRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Catalog> getAll() {
        return catalogRepository.getAll();
    }

    @Override
    public Catalog getById(Long id) {
        return catalogRepository.getById(id)
                .orElseThrow(() -> new CatalogNotFoundException(id));
    }

    @Override
    public Catalog create(Catalog catalog) {
        if (catalogRepository.findByName(catalog.getName()).isPresent()) {
            throw new DuplicateCatalogNameException(catalog.getName());
        }
        return catalogRepository.save(catalog);
    }

    @Override
    public Catalog update(Long id, Catalog catalog) {
        getById(id);
        catalog.setId(id);
        catalogRepository.findByName(catalog.getName())
                .map(Catalog::getId)
                .filter(existingId -> !existingId.equals(id))
                .ifPresent(existingId -> {
                    throw new DuplicateCatalogNameException(catalog.getName());
                });
        catalogRepository.update(catalog);
        return catalog;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        long productCount = productRepository.countByCatalogId(id);
        if (productCount > 0) {
            throw new CatalogNotEmptyException(id, productCount);
        }
        catalogRepository.deleteById(id);
    }
}
