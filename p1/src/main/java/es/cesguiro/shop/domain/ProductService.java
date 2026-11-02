package es.cesguiro.shop.domain;

import java.util.List;

/**
 * Interfaz del servicio de productos del dominio.
 */
public interface ProductService {

    List<Product> getAll();
}
