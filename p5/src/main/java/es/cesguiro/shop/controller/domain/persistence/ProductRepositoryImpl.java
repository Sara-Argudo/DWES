package es.cesguiro.shop.controller.domain.persistence;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.model.Money;
import es.cesguiro.shop.controller.domain.model.Product;
import es.cesguiro.shop.controller.domain.model.Stock;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import es.cesguiro.shop.controller.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementación de la interfaz del repositorio de productos del dominio.
 * Trabaja con la entidad del DAO y traduce al modelo de dominio, componiendo
 * los value objects `Money` y `Stock` al leer de la base de datos y leyendo
 * su valor al escribir.
 * Traduce `page`/`size` (idioma del dominio) a `limit`/`offset` (idioma de SQL).
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductDao productDao;

    public ProductRepositoryImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Optional<Product> getById(Long id) {
        return productDao.getById(id).map(this::toModel);
    }

    @Override
    public List<Product> getByCatalogId(Long catalogId, int page, int size) {
        int limit = size;
        int offset = page * size;
        return productDao.getPageByCatalogId(catalogId, limit, offset).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public long countByCatalogId(Long catalogId) {
        return productDao.countByCatalogId(catalogId);
    }

    @Override
    public Product save(Product product) {
        Long id = productDao.save(toEntity(product));
        product.setId(id);
        return product;
    }

    @Override
    public void update(Product product) {
        productDao.update(toEntity(product));
    }

    @Override
    public void deleteById(Long id) {
        productDao.deleteById(id);
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.getId(),
                product.getCatalogId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().amount(),
                product.getStock().units()
        );
    }

    private Product toModel(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getCatalogId(),
                entity.getName(),
                entity.getDescription(),
                Money.of(entity.getPrice()),
                Stock.of(entity.getStock())
        );
    }
}
