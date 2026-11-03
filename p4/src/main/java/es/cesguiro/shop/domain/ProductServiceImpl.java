package es.cesguiro.shop.domain;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de productos.
 * Depende de las interfaces de los repositorios (definidas en domain), no de sus
 * implementaciones, y completa las FK que declara el contrato: 404 cuando el
 * catálogo de los datos no existe (alta o modificación).
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;

    public ProductServiceImpl(ProductRepository productRepository, CatalogRepository catalogRepository) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
    }

    @Override
    public Product getById(Long id) {
        return productRepository.getById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Product create(Product product) {
        requireCatalog(product.getCatalogId());
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product product) {
        productRepository.getById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        requireCatalog(product.getCatalogId());
        product.setId(id);
        productRepository.update(product);
        return product;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getByCatalogId(Long catalogId, int page, int size) {
        return productRepository.getByCatalogId(catalogId, page, size);
    }

    @Override
    public long countByCatalogId(Long catalogId) {
        return productRepository.countByCatalogId(catalogId);
    }

    /** 404 si el catálogo de los datos no existe. */
    private void requireCatalog(Long catalogId) {
        catalogRepository.getById(catalogId)
                .orElseThrow(() -> new CatalogNotFoundException(catalogId));
    }
}
