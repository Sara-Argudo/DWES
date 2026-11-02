package es.cesguiro.shop.catalog.infrastructure.persistence;

import es.cesguiro.shop.catalog.domain.Product;
import es.cesguiro.shop.shared.domain.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductDataJpa extends JpaRepository<Product, Long> {

    Page<Product> findByCatalogIdOrderByIdAsc(long catalogId, Pageable pageable);

    boolean existsByCatalogId(long catalogId);

    /**
     * Decrementa las unidades reserve disponibles solo si quedan suficientes.
     * Devuelve 1 si la reserva se aplicó y 0 si no: la atomicidad la maneja
     * la transacción del pedido (si falla ninguna reserva, todo se deshace).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Product p
               SET p.stock = p.stock - :units
             WHERE p.id = :id
               AND :units <= p.stock
            """)
    int reserveStock(@Param("id") long id, @Param("units") Stock units);

    /**
     * Devuelve al stock las unidades reservadas de un pedido cancelado.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Product p
               SET p.stock = p.stock + :units
             WHERE p.id = :id
            """)
    int releaseStock(@Param("id") long id, @Param("units") Stock units);
}
