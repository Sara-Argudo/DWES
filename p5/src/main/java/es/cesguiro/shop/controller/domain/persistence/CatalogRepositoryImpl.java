package es.cesguiro.shop.controller.domain.persistence;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementación de la interfaz del repositorio de catálogos del dominio.
 * Trabaja con la entidad del DAO y traduce al modelo de dominio.
 */
@Repository
public class CatalogRepositoryImpl implements CatalogRepository {

    private final CatalogDao catalogDao;

    public CatalogRepositoryImpl(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    @Override
    public List<Catalog> getAll() {
        return catalogDao.getAll().stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public Optional<Catalog> getById(Long id) {
        return catalogDao.getById(id).map(this::toModel);
    }

    @Override
    public Optional<Catalog> findByName(String name) {
        return catalogDao.findByName(name).map(this::toModel);
    }

    @Override
    public Catalog save(Catalog catalog) {
        Long id = catalogDao.save(toEntity(catalog));
        catalog.setId(id);
        return catalog;
    }

    @Override
    public void update(Catalog catalog) {
        catalogDao.update(toEntity(catalog));
    }

    @Override
    public void deleteById(Long id) {
        catalogDao.deleteById(id);
    }

    private CatalogEntity toEntity(Catalog catalog) {
        return new CatalogEntity(
                catalog.getId(),
                catalog.getName(),
                catalog.getDescription()
        );
    }

    private Catalog toModel(CatalogEntity entity) {
        return new Catalog(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}
