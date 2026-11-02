package es.cesguiro.shop.domain;

import java.util.List;

/**
 * Interfaz del repositorio que el dominio necesita.
 * La implementación vive en la capa de persistencia (inversión de dependencias);
 * aquí no se menciona JDBC ni ninguna tecnología de acceso a datos.
 */
public interface ProductRepository {

    List<Product> getAll();
}
