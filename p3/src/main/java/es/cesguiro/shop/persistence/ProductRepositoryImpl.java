package es.cesguiro.shop.persistence;

import java.util.List;

import es.cesguiro.shop.domain.Product;
import es.cesguiro.shop.domain.ProductRepository;
import org.springframework.stereotype.Repository;

/**
 * Implementación de la interfaz del repositorio del dominio.
 * Trabaja con la entidad del DAO y traduce al modelo de dominio.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductDao productDao;

    public ProductRepositoryImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public List<Product> getAll() {
        return productDao.getAll().stream()
                .map(this::toModel)
                .toList();
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
