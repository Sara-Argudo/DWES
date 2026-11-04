package es.cesguiro.shop.controller.domain.model;

import es.cesguiro.shop.controller.domain.exception.InvalidValueException;

/**
 * Value Object del stock: entero no negativo. Valida en su construcción y
 * lanza la excepción de dominio propia: un stock imposible nunca llega a la
 * persistencia, venga o no la petición validada por el contrato.
 */
public class Stock {

    private final int units;

    private Stock(int units) {
        if (units < 0) {
            throw new InvalidValueException("Stock value cannot be negative");
        }
        this.units = units;
    }

    /** Construye un `Stock` y valida la invariante antes de devolverlo. */
    public static Stock of(Integer units) {
        if (units == null) {
            throw new InvalidValueException("Stock value is required");
        }
        return new Stock(units);
    }

    public int units() {
        return units;
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
