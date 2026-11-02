package es.cesguiro.shop.catalog.controller;

import java.util.List;

import es.cesguiro.shop.catalog.application.CatalogService;
import es.cesguiro.shop.catalog.controller.dto.CatalogDtos.CatalogDto;
import es.cesguiro.shop.catalog.controller.dto.CatalogDtos.CatalogRequest;
import es.cesguiro.shop.catalog.domain.Catalog;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * CRUD de catálogos. Lecturas públicas; escrituras solo para ADMIN
 * (la regla de rutas vive en Spring Security, módulo users).
 */
@RestController
@RequestMapping("/api/catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<CatalogDto> getAll() {
        return catalogService.getAll().stream()
                .map(CatalogDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CatalogDto getById(@PathVariable long id) {
        return CatalogDto.from(catalogService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CatalogDto> create(@Valid @RequestBody CatalogRequest request,
                                             UriComponentsBuilder uri) {
        Catalog catalog = catalogService.create(request.name(), request.description());
        var location = uri.path("/api/catalogs/{id}").buildAndExpand(catalog.getId()).toUri();
        return ResponseEntity.created(location).body(CatalogDto.from(catalog));
    }

    @PutMapping("/{id}")
    public CatalogDto update(@PathVariable long id, @Valid @RequestBody CatalogRequest request) {
        return CatalogDto.from(catalogService.update(id, request.name(), request.description()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
