package es.cesguiro.shop.catalog.infrastructure.persistence;

import java.util.List;

import es.cesguiro.shop.catalog.domain.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogDataJpa extends JpaRepository<Catalog, Long> {

    List<Catalog> findByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);

    java.util.Optional<Catalog> findByNameIgnoreCase(String name);
}
