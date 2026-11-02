package es.cesguiro.shop.orders.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.cesguiro.shop.orders.application.OrderService;
import es.cesguiro.shop.orders.domain.Order;
import es.cesguiro.shop.orders.domain.OrderLine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Endpoints del módulo de pedidos. El usuario que crea el pedido viene
 * del token; no hay campo userId en el contrato. Las reglas de pertenencia
 * (propietario o ADMIN) las aplica el servicio, no esta capa.
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record OrderLineDto(long productId, int quantity, BigDecimal unitPrice) {
        static OrderLineDto from(OrderLine line) {
            return new OrderLineDto(
                    line.getProductId(),
                    line.getQuantity(),
                    line.getUnitPrice().amount()
            );
        }
    }

    public record OrderDto(
            long id,
            long userId,
            String state,
            Instant createdAt,
            List<OrderLineDto> lines,
            BigDecimal total
    ) {
        public static OrderDto from(Order order) {
            List<OrderLineDto> mapped = order.getLines().stream()
                    .map(OrderLineDto::from)
                    .toList();
            return new OrderDto(
                    order.getId(),
                    order.getUserId(),
                    order.getState().name(),
                    order.getCreatedAt(),
                    mapped,
                    order.total().amount()
            );
        }
    }

    public record CreateOrderRequest(
            @NotEmpty(message = "An order requires at least one line")
            List<@Valid OrderLinePayload> lines
    ) {
    }

    public record OrderLinePayload(
            @NotNull(message = "productId is required") Long productId,
            @Min(value = 1, message = "quantity must be at least 1") int quantity
    ) {
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request,
                                           UriComponentsBuilder uri) {
        Order order = orderService.createOrder(
                new OrderService.CreateRequest(
                        request.lines().stream()
                                .map(line -> new OrderService.OrderLineRequest(line.productId(), line.quantity()))
                                .toList()
                )
        );
        var location = uri.path("/api/orders/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(OrderDto.from(order));
    }

    @GetMapping("/orders/{id}")
    public OrderDto getById(@PathVariable long id) {
        return OrderDto.from(orderService.getById(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancel(@PathVariable long id) {
        return OrderDto.from(orderService.cancel(id));
    }

    @GetMapping("/me/orders")
    public List<OrderDto> myOrders() {
        return orderService.myOrders().stream()
                .map(OrderDto::from)
                .toList();
    }
}
