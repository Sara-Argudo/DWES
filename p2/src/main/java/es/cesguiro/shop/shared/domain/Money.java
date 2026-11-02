package es.cesguiro.shop.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import es.cesguiro.shop.shared.exception.InvalidValueException;

/**
 * Value Object del dinero: mayor que cero y de dos decimales como precisión monetaria.
 */
public class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal candidate) {
        if (candidate == null) {
            throw new InvalidValueException("Money value is required");
        }
        // precisión monetaria: como mucho dos decimales
        if (candidate.stripTrailingZeros().scale() > 2) {
            throw new InvalidValueException("Money value must have at most two decimals");
        }
        if (candidate.signum() <= 0) {
            throw new InvalidValueException("Money value must be greater than zero");
        }
        return new Money(candidate.setScale(2, RoundingMode.UNNECESSARY));
    }

    public static Money of(String amount) {
        try {
            return of(new BigDecimal(amount));
        } catch (NumberFormatException e) {
            throw new InvalidValueException("Money value must be a valid decimal");
        }
    }

    public static Money of(double amount) {
        return of(new BigDecimal(String.valueOf(amount)));
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.UNNECESSARY));
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount).setScale(2, RoundingMode.UNNECESSARY));
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
