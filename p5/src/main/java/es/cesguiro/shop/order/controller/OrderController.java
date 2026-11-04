package es.cesguiro.shop.order.controller;

import org.springframework.web.bind.annotation.RestController;

/**
 * Andamiajo del módulo de pedidos dentro del monolito modular.
 * Los endpoints de pedidos (API pública incluida) llegan en el hito 06 —
 * módulos: pedidos y API pública. El módulo de catálogo (`controller`) no
 * depende de este módulo, y este no depende del de catálogo.
 */
@RestController
public class OrderController {
}
