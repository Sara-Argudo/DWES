package es.cesguiro.shop.controller.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.controller.domain.exception.CatalogNotEmptyException;
import es.cesguiro.shop.controller.domain.exception.CatalogNotFoundException;
import es.cesguiro.shop.controller.domain.exception.DuplicateCatalogNameException;
import es.cesguiro.shop.controller.domain.model.Catalog;
import es.cesguiro.shop.controller.domain.repository.CatalogRepository;
import es.cesguiro.shop.controller.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias del servicio de catálogos con un DOBLE del repositorio:
 * las reglas de estado del contrato (404 y 409) se aplican en el servicio,
 * sin Spring y sin base de datos.
 */
@DisplayName("CatalogService: reglas de estado con un doble del repositorio")
@ExtendWith(MockitoExtension.class)
class CatalogServiceTests {

    @Mock
    private CatalogRepository catalogRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Test
    @DisplayName("getById falla con la excepción de dominio si el catálogo no existe")
    void getByIdFailsForUnknownCatalog() {
        when(catalogRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getById(99L))
                .isInstanceOf(CatalogNotFoundException.class);
    }

    @Test
    @DisplayName("create falla si el nombre ya lo usa otro catálogo y no llega a guardar")
    void createRejectsDuplicateName() {
        when(catalogRepository.findByName("Periféricos"))
                .thenReturn(Optional.of(new Catalog(1L, "Periféricos", "duplicado")));

        assertThatThrownBy(() -> catalogService.create(new Catalog(null, "Periféricos", "x")))
                .isInstanceOf(DuplicateCatalogNameException.class);

        verify(catalogRepository, never()).save(org.mockito.ArgumentMatchers.any(Catalog.class));
    }

    @Test
    @DisplayName("create guarda cuando el nombre está libre")
    void createSavesWhenNameIsFree() {
        Catalog toCreate = new Catalog(null, "Novedades", "nuevo");
        when(catalogRepository.findByName("Novedades")).thenReturn(Optional.empty());
        when(catalogRepository.save(toCreate)).thenAnswer(invocation -> {
            Catalog saved = invocation.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        Catalog created = catalogService.create(toCreate);

        assertThat(created.getId()).isEqualTo(4L);
    }

    @Test
    @DisplayName("update falla si el catálogo no existe")
    void updateFailsForUnknownCatalog() {
        when(catalogRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.update(99L, new Catalog(null, "X", "y")))
                .isInstanceOf(CatalogNotFoundException.class);
    }

    @Test
    @DisplayName("update falla si otro catálogo distinto usa ya el nombre")
    void updateRejectsNameUsedByAnotherCatalog() {
        when(catalogRepository.getById(2L)).thenReturn(Optional.of(new Catalog(2L, "viejo", "y")));
        when(catalogRepository.findByName("Periféricos"))
                .thenReturn(Optional.of(new Catalog(1L, "Periféricos", "otro")));

        assertThatThrownBy(() -> catalogService.update(2L, new Catalog(null, "Periféricos", "z")))
                .isInstanceOf(DuplicateCatalogNameException.class);

        verify(catalogRepository, never()).update(org.mockito.ArgumentMatchers.any(Catalog.class));
    }

    @Test
    @DisplayName("update permite conservar el nombre propio")
    void updateKeepsOwnName() {
        when(catalogRepository.getById(1L)).thenReturn(Optional.of(new Catalog(1L, "Periféricos", "y")));
        when(catalogRepository.findByName("Periféricos"))
                .thenReturn(Optional.of(new Catalog(1L, "Periféricos", "y")));

        Catalog updated = catalogService.update(1L, new Catalog(null, "Periféricos", "nueva"));

        assertThat(updated.getId()).isEqualTo(1L);
        verify(catalogRepository).update(updated);
    }

    @Test
    @DisplayName("delete falla si el catálogo no existe")
    void deleteFailsForUnknownCatalog() {
        when(catalogRepository.getById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.delete(99L))
                .isInstanceOf(CatalogNotFoundException.class);
    }

    @Test
    @DisplayName("delete falla si el catálogo tiene productos y no borra nada")
    void deleteRejectsCatalogWithProducts() {
        when(catalogRepository.getById(1L)).thenReturn(Optional.of(new Catalog(1L, "Periféricos", "y")));
        when(productRepository.countByCatalogId(1L)).thenReturn(12L);

        assertThatThrownBy(() -> catalogService.delete(1L))
                .isInstanceOf(CatalogNotEmptyException.class);

        verify(catalogRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("delete borra cuando el catálogo está vacío")
    void deletesEmptyCatalog() {
        when(catalogRepository.getById(3L)).thenReturn(Optional.of(new Catalog(3L, "Vacío", "y")));
        when(productRepository.countByCatalogId(3L)).thenReturn(0L);

        catalogService.delete(3L);

        verify(catalogRepository).deleteById(3L);
    }

    @Test
    @DisplayName("getAll lista los catálogos del repositorio")
    void getAllLists() {
        when(catalogRepository.getAll()).thenReturn(List.of(new Catalog(1L, "Periféricos", "y")));

        assertThat(catalogService.getAll()).hasSize(1);
    }
}
