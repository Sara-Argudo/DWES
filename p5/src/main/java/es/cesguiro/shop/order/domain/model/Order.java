package es.cesguiro.shop.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo del dominio del módulo de pedidos: un pedido de la tienda.
 * Andamiajo del monolito modular: la funcionalidad (endpoints, servicio,
 * persistencia) llega en el hito 06 — módulos: pedidos y API pública.
 * Referencia sus líneas por identificador de producto; el precio se
 * capturará al reservar el stock (no el precio actual del producto).
 */
public class Order {

    private Long id;
    private Long userId;
    private String state;
    private Instant createdAt;
    private List<OrderDetail> details = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Long userId, String state, Instant createdAt, List<OrderDetail> details) {
        this.id = id;
        this.userId = userId;
        this.state = state;
        this.createdAt = createdAt;
        this.details = details == null ? new ArrayList<>() : details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }
}
