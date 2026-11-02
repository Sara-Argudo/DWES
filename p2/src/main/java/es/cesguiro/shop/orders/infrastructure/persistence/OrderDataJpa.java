package es.cesguiro.shop.orders.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import es.cesguiro.shop.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDataJpa extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(long userId);

    Optional<Order> findById(long id);
}
