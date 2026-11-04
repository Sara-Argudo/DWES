package es.cesguiro.shop.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase de arranque de la tienda. Vive en la carpeta `persistence`, fuera de
 * los módulos (`controller` y `order`), que es donde Spring Boot la espera
 * por debajo de ningún módulo en concreto. Los componentes de los módulos se
 * descubren porque el escaneo arranca en el paquete raíz `es.cesguiro.shop`.
 */
@SpringBootApplication(scanBasePackages = "es.cesguiro.shop")
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
