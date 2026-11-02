package es.cesguiro.shop.orders.application;

import java.time.Instant;
import java.util.List;

import es.cesguiro.shop.catalog.api.CatalogApi;
import es.cesguiro.shop.orders.domain.Order;
import es.cesguiro.shop.orders.domain.OrderLine;
import es.cesguiro.shop.orders.exception.OrderNotFoundException;
import es.cesguiro.shop.orders.infrastructure.persistence.OrderDataJpa;
import es.cesguiro.shop.shared.domain.Money;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de pedidos. La identidad del comprador viene del token
 * (principal del SecurityContext), nunca del cuerpo de la petición; la
 * pertenencia (propietario o ADMIN) se comprueba aquí, no en la capa web.
 */
@Service
public class OrderService {

    private final OrderDataJpa orders;
    private final CatalogApi catalogApi;

    public OrderService(OrderDataJpa orders, CatalogApi catalogApi) {
        this.orders = orders;
        this.catalogApi = catalogApi;
    }

    public record OrderLineRequest(long productId, int quantity) {
    }

    public record CreateRequest(List<OrderLineRequest> lines) {
    }

    /**
     * Crear un pedido es una única transacción: se capturan los precios
     * y se reserva el stock de todas las líneas; si alguna reserva
     * falla (409), el rollback deja el stock y las filas como estaban.
     */
    @Transactional
    public Order createOrder(CreateRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new IllegalArgumentException("An order requires at least one line");
        }
        Instant createdAt = Instant.now();
        Order order = new Order(currentUserId(), createdAt);
        for (OrderLineRequest lineRequest : request.lines()) {
            Money priceAtReservation = catalogApi.priceOf(lineRequest.productId());
            OrderLine line = new OrderLine(
                    lineRequest.productId(),
                    lineRequest.quantity(),
                    priceAtReservation
            );
            line.belongsTo(order);
            order.addLine(line);
        }
        for (OrderLine line : order.getLines()) {
            catalogApi.reserve(line.getProductId(), line.getQuantity());
        }
        return orders.save(order);
    }

    @Transactional(readOnly = true)
    public Order getById(long id) {
        Order order = orders.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        authorizeCurrent(order);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders() {
        return orders.findByUserIdOrderByCreatedAtDesc(currentUserId());
    }

    /**
     * Cancelar libera el stock de todas sus líneas (transacción completa).
     */
    @Transactional
    public Order cancel(long orderId) {
        Order order = getById(orderId);
        order.cancel();
        for (var line : order.getLines()) {
            catalogApi.release(line.getProductId(), line.getQuantity());
        }
        return order;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    /**
     * Un pedido solo puede verse o cancelarse por su propietario o por ADMIN.
     */
    private void authorizeCurrent(Order order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        boolean isOwner = order.getUserId() != null && order.getUserId().equals(currentUserId);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not your order");
        }
    }
}
