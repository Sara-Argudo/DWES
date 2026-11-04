package es.cesguiro.shop.controller.domain.persistence;

/**
 * Entidad de la capa de persistencia: refleja la tabla products.
 * El precio y el stock llegan crudos de la base de datos: aquí no hay
 * value objects, cuya construcción corresponde al dominio.
 */
public class ProductEntity {

    private Long id;
    private Long catalogId;
    private String name;
    private String description;
    private java.math.BigDecimal price;
    private int stock;

    public ProductEntity() {
    }

    public ProductEntity(Long id, Long catalogId, String name, String description,
                         java.math.BigDecimal price, int stock) {
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

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
