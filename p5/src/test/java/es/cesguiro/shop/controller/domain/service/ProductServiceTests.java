package es.cesguiro.shop.controller.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.ProductNotFoundException;
import es.cesguiro.shop.controller.domain.model.Money;
import es.cesguiro.shop.controller.domain.model.Product;
import es.cesguiro.shop.controller.domain.model.Stock;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import es.cesguiro.shop.controller.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del servicio de productos con un DOBLE del repositorio.
 * Comprueba que la creación y la modificación construyen el modelo a través
 * de los value objects, que un producto con `catalogId` desconocido no se
 * llega a crear y que un producto inexistente al modificar o borrar falla
 * con la excepción de dominio correspondiente.
 */
@DisplayName("ProductService: value objects y FK con un doble del repositorio")
@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product productWithId() {
        return new Product(1L, 1L, "Teclado mecánico", "Mecánico, inalámbrico",
                Money.of("54.90"), Stock.of(20));
    }

    @Test
    @DisplayName("getById falla con la excepción de dominio si el producto no existe")
    void getByIdFailsForUnknownProduct() {
        when(productRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("create construye el producto a través de los value objects")
    void createBuildsThroughValueObjects() {
        when(catalogRepository.getById(1L)).thenReturn(Optional.of(new es.cesguiro.shop.controller.domain.model.Catalog(1L, "Periféricos", "y")));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.create(new ProductService.ProductInput(
                1L, "Cable HDMI", "2 m, 8K", new BigDecimal("14.90"), 45));

        assertThat(created.getPrice()).isEqualTo(Money.of("14.90"));
        assertThat(created.getStock()).isEqualTo(Stock.of(45));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create rechaza en el dominio un precio imposible (invariante del Money)")
    void createRejectsImpossiblePrice() {
        assertThatThrownBy(() -> productService.create(new ProductService.ProductInput(
                1L, "Cable HDMI", "2 m, 8K", new BigDecimal("0"), 45)))
                .isInstanceOf(es.cesguiro.shop.controller.domain.exception.InvalidValueException.class);

        verify(productRepository, never()).save(any(Product.class));
        verify(catalogRepository, never()).getById(any());
    }

    @Test
    @DisplayName("create rechaza en el dominio un stock negativo (invariante del Stock)")
    void createRejectsNegativeStock() {
        assertThatThrownBy(() -> productService.create(new ProductService.ProductInput(
                1L, "Cable HDMI", "2 m, 8K", new BigDecimal("14.90"), -1)))
                .isInstanceOf(es.cesguiro.shop.controller.domain.exception.InvalidValueException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("create no llega a persistir si el catalogId es desconocido")
    void createFailsForUnknownCatalog() {
        when(catalogRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(new ProductService.ProductInput(
                99L, "Cable HDMI", "2 m, 8K", new BigDecimal("14.90"), 45)))
                .isInstanceOf(CatalogNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("update falla si el producto no existe")
    void updateFailsForUnknownProduct() {
        when(productRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, new ProductService.ProductInput(
                1L, "Cable HDMI", "2 m, 8K", new BigDecimal("14.90"), 45)))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("update no llega a persistir si el catalogId es desconocido")
    void updateFailsForUnknownCatalog() {
        when(productRepository.getById(1L)).thenReturn(Optional.of(productWithId()));
        when(catalogRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, new ProductService.ProductInput(
                99L, "Cable HDMI", "2 m, 8K", new BigDecimal("14.90"), 45)))
                .isInstanceOf(CatalogNotFoundException.class);

        verify(productRepository, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("update construye el modelo modificado a través de los value objects")
    void updateBuildsThroughValueObjects() {
        when(productRepository.getById(1L)).thenReturn(Optional.of(productWithId()));
        when(catalogRepository.getById(1L)).thenReturn(Optional.of(new es.cesguiro.shop.controller.domain.model.Catalog(1L, "Periféricos", "y")));

        Product updated = productService.update(1L, new ProductService.ProductInput(
                1L, "Cable HDMI 2.1", "2 m, 8K", new BigDecimal("15.90"), 40));

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getPrice()).isEqualTo(Money.of("15.90"));
        assertThat(updated.getStock()).isEqualTo(Stock.of(40));
        verify(productRepository).update(updated);
    }

    @Test
    @DisplayName("delete falla si el producto no existe")
    void deleteFailsForUnknownProduct() {
        when(productRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("delete borra cuando el producto existe")
    void deleteKnownProduct() {
        when(productRepository.getById(1L)).thenReturn(Optional.of(productWithId()));

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}
