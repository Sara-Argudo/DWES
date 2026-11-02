package es.cesguiro.shop.catalog.domain;

import java.math.BigDecimal;

import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.MoneyConverter;
import es.cesguiro.shop.shared.domain.Stock;
import es.cesguiro.shop.shared.domain.StockConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalog_id", nullable = false)
    private Long catalogId;

    private String name;
    private String description;

    @Convert(converter = MoneyConverter.class)
    @Column(precision = 8, scale = 2, nullable = false)
    private Money price;

    @Convert(converter = StockConverter.class)
    @Column(nullable = false)
    private Stock stock;

    protected Product() {
    }

    public Product(Long catalogId, String name, String description, Money price, Stock stock) {
        this.catalogId = catalogId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCatalogId() {
        return catalogId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}
