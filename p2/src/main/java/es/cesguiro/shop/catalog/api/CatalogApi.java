package es.cesguiro.shop.catalog.api;

import es.cesguiro.shop.shared.domain.Money;

/**
 * API pública del módulo catalog, consumida por el módulo orders.
 * Los pedidos nunca tocan las entidades internas del catálogo: consultan
 * el precio por este contrato y reservan o liberan stock por aquí.
 */
public interface CatalogApi {

    /**
     * Precio vigente del producto. Lanza 404 si no existe.
     */
    Money priceOf(long productId);

    /**
     * Reserva unidades si hay stock suficiente; 409 si no las hay.
     * La atomicidad la da la transacción de quien llama.
     */
    void reserve(long productId, int units);

    /**
     * Devuelve al stock las unidades reservadas (al cancelar un pedido).
     */
    void release(long productId, int units);
}
