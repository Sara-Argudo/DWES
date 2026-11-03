package es.cesguiro.shop.persistence;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.domain.Product;
import es.cesguiro.shop.domain.ProductRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementación de la interfaz del repositorio del dominio.
 * Trabaja con la entidad del DAO y traduce al modelo de dominio.
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
                product.getPrice(),
                product.getStock()
        );
    }

    private Product toModel(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getCatalogId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock()
        );
    }
}
