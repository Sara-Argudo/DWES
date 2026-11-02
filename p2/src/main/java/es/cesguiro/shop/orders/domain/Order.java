package es.cesguiro.shop.orders.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import es.cesguiro.shop.orders.exception.OrderAlreadyCancelledException;
import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.exception.InvalidValueException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(Long userId, Instant createdAt) {
        this.userId = userId;
        this.createdAt = createdAt;
        this.state = OrderState.CREATED;
    }

    /**
     * Regla de negocio: un pedido tiene al menos una línea; la cantidad
     * de cada línea es 1 o más (el contrato lo valida antes de llegar).
     */
    public void addLine(OrderLine line) {
        if (line.getQuantity() < 1) {
            throw new InvalidValueException("Order line quantity must be at least 1");
        }
        this.lines.add(line);
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * El total es la suma de cantidad por precio de línea.
     */
    public Money total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            sum = sum.add(line.getUnitPrice().amount().multiply(new BigDecimal(line.getQuantity())));
        }
        return Money.of(sum);
    }

    /**
     * Única transición permitida: CREATED -> CANCELLED.
     * Cancelar un pedido ya cancelado es un error (409).
     */
    public void cancel() {
        if (this.state == OrderState.CANCELLED) {
            throw new OrderAlreadyCancelledException(this.id);
        }
        this.state = OrderState.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderState getState() {
        return state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
