package es.cesguiro.shop.controller.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import es.cesguiro.shop.controller.domain.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del value object Money: construido dentro de rango y
 * rechazado fuera. Sin Spring, sin base de datos: la invariante vive en el
 * value object, no en la validación de la petición.
 */
@DisplayName("Money: el precio es mayor que cero y de una escala máxima de 2 decimales")
class MoneyTests {

    @Test
    @DisplayName("Acepta precios válidos dentro de rango")
    void acceptsValidPrices() {
        assertThat(Money.of(new BigDecimal("0.01")).amount())
                .isEqualByComparingTo(new BigDecimal("0.01"));
        assertThat(Money.of(new BigDecimal("999999.99")).amount())
                .isEqualByComparingTo(new BigDecimal("999999.99"));

        // un precio de un decimal se normaliza a la escala monetaria de 2
        assertThat(Money.of(new BigDecimal("24.9")).amount())
                .isEqualTo(Money.of("24.90").amount());
        assertThat(Money.of("24.9")).isEqualTo(Money.of("24.90"));
    }

    @Test
    @DisplayName("Rechaza nulo, cero, negativos y más de dos decimales")
    void rejectsInvalidPrices() {
        assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("0.00")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("4.555")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of("no-es-un-precio"))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("La igualdad compara el valor monetario, no su representación")
    void equalsComparesValue() {
        assertThat(Money.of("10.0")).isEqualTo(Money.of("10.00"));
        assertThat(Money.of("10.50")).isNotEqualTo(Money.of("10.05"));
    }
}
