package es.cesguiro.shop.controller.domain.model;

/**
 * Modelo del dominio: un producto de la tienda.
 * Referencia su catálogo por identificador (catalogId) y COMPONE los value
 * objects del negocio: el precio es un `Money` (mayor que cero, escala 2) y
 * el stock un `Stock` (no negativo). El estado imposible no puede construirse.
 */
public class Product {

    private Long id;
    private Long catalogId;
    private String name;
    private String description;
    private Money price;
    private Stock stock;

    public Product() {
    }

    public Product(Long id, Long catalogId, String name, String description, Money price, Stock stock) {
        this.id = id;
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

    public void setCatalogId(Long catalogId) {
        this.catalogId = catalogId;
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
