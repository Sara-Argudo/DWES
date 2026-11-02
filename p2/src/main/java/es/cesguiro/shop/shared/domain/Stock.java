package es.cesguiro.shop.shared.domain;

import es.cesguiro.shop.shared.exception.InvalidValueException;

/**
 * Value Object del stock: nunca negativo.
 */
public class Stock {

    private final int units;

    private Stock(int units) {
        if (units < 0) {
            throw new InvalidValueException("Stock value cannot be negative");
        }
        this.units = units;
    }

    public static Stock of(int units) {
        return new Stock(units);
    }

    public int units() {
        return units;
    }

    public boolean hasAtLeast(int quantity) {
        return units >= quantity;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Stock stock && stock.units == units;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(units);
    }

    @Override
    public String toString() {
        return String.valueOf(units);
    }
}
