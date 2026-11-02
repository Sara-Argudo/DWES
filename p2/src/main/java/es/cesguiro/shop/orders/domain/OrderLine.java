package es.cesguiro.shop.orders.domain;

import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.MoneyConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private int quantity;

    /**
     * Regla de negocio: el precio capturado al reservar el stock,
     * no el precio actual del producto.
     */
    @Convert(converter = MoneyConverter.class)
    @Column(name = "unit_price", precision = 8, scale = 2, nullable = false)
    private Money unitPrice;

    protected OrderLine() {
    }

    public OrderLine(Long productId, int quantity, Money unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void belongsTo(Order order) {
        this.order = order;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}
