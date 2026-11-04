package es.cesguiro.shop.controller.domain.service;

import java.util.List;

import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.model.Money;
import es.cesguiro.shop.controller.domain.model.Product;
import es.cesguiro.shop.controller.domain.model.Stock;
import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.ProductNotFoundException;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import es.cesguiro.shop.controller.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de productos.
 * Depende de las interfaces de los repositorios (definidas en domain), no de
 * sus implementaciones, y completa las FK que declara el contrato: 404 cuando
 * el catálogo de los datos no existe (alta o modificación).
 *
 * La creación y la modificación construyen PRIMERO los value objects `Money`
 * y `Stock`: si una invariante falla, el estado inválido nunca llega a la
 * persistencia.
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
    public Product create(ProductInput input) {
        // los value objects validan la invariante antes de tocar cualquier otra cosa
        Money price = Money.of(input.price());
        Stock stock = Stock.of(input.stock());
        requireCatalog(input.catalogId());
        Product product = new Product(null, input.catalogId(), input.name(), input.description(), price, stock);
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, ProductInput input) {
        getById(id);
        Money price = Money.of(input.price());
        Stock stock = Stock.of(input.stock());
        requireCatalog(input.catalogId());
        Product product = new Product(id, input.catalogId(), input.name(), input.description(), price, stock);
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
