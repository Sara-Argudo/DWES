package es.cesguiro.shop.order.domain.model;

import java.math.BigDecimal;

/**
 * Modelo del dominio del módulo de pedidos: una línea de un pedido.
 * Andamiajo del monolito modular (la funcionalidad llega en el hito 06).
 * El precio capturado por unidad vive aquí; el value object `Money` es del
 * módulo de catálogo y no se reutiliza entre módulos (independencia entre
 * `controller` y `order` en el monolito modular).
 */
public class OrderDetail {

    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderDetail() {
    }

    public OrderDetail(Long id, Long orderId, Long productId, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
