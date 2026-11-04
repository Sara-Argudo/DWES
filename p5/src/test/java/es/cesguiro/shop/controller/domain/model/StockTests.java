package es.cesguiro.shop.controller.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.cesguiro.shop.controller.domain.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del value object Stock: construido dentro de rango y
 * rechazado fuera. Sin Spring, sin base de datos: la invariante vive en el
 * value object, no en la validación de la petición.
 */
@DisplayName("Stock: el stock es un entero no negativo")
class StockTests {

    @Test
    @DisplayName("Acepta cero y cantidades positivas dentro de rango")
    void acceptsValidStocks() {
        assertThat(Stock.of(0).units()).isZero();
        assertThat(Stock.of(100000).units()).isEqualTo(100000);
    }

    @Test
    @DisplayName("Rechaza negativos y nulo")
    void rejectsInvalidStocks() {
        assertThatThrownBy(() -> Stock.of(-1))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Stock.of((Integer) null))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("La igualdad compara las unidades")
    void equalsComparesUnits() {
        assertThat(Stock.of(20)).isEqualTo(Stock.of(20));
        assertThat(Stock.of(20)).isNotEqualTo(Stock.of(21));
    }
}
