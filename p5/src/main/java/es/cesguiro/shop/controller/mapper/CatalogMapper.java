package es.cesguiro.shop.controller.mapper;

import es.cesguiro.shop.controller.domain.model.Catalog;

/**
 * Mapper del módulo controller: traduce entre el DTO del contrato
 * (`Catalog` y `CatalogRequest`, generados en controller.dto) y el modelo
 * del dominio. Los controladores delegan en él para no traducir a mano.
 */
public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static es.cesguiro.shop.controller.dto.Catalog toApi(Catalog catalog) {
        return new es.cesguiro.shop.controller.dto.Catalog(
                catalog.getId(),
                catalog.getName(),
                catalog.getDescription()
        );
    }

    public static Catalog toDomain(es.cesguiro.shop.controller.dto.CatalogRequest request) {
        return new Catalog(
                null,
                request.getName(),
                request.getDescription()
        );
    }
}
