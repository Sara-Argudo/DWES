package es.cesguiro.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import es.cesguiro.shop.shared.domain.Money;
import es.cesguiro.shop.shared.domain.Stock;
import es.cesguiro.shop.shared.exception.InvalidValueException;
import org.junit.jupiter.api.Test;

/**
 * Invariantes de los value objects Money y Stock (sin contenedor).
 */
class ValueObjectsTests {

    @Test
    void moneyRequiresPositiveValueWithTwoDecimals() {
        assertThat(Money.of("4.50").amount()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(Money.of(new BigDecimal("4.5")))
                .isEqualTo(Money.of("4.50"));

        assertThatThrownBy(() -> Money.of(new BigDecimal("0.00")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(new BigDecimal("4.555")))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of((java.math.BigDecimal) null))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void moneySupportsOperationsForOrderTotals() {
        Money price = Money.of("10.00");
        Money twoLines = price.multiply(3).add(Money.of("2.50"));
        assertThat(twoLines.amount()).isEqualByComparingTo(new BigDecimal("32.50"));
    }

    @Test
    void stockIsNeverNegative() {
        assertThat(Stock.of(0).units()).isZero();
        assertThat(Stock.of(12).hasAtLeast(5)).isTrue();
        assertThat(Stock.of(4).hasAtLeast(5)).isFalse();
        assertThatThrownBy(() -> Stock.of(-1))
                .isInstanceOf(InvalidValueException.class);
    }
}
