package es.cesguiro.shop.controller.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import es.cesguiro.shop.controller.domain.exception.InvalidValueException;

/**
 * Value Object del precio: mayor que cero y con una escala máxima de 2 decimales.
 * Valida en su construcción y lanza la excepción de dominio propia: cualquier
 * código que construya un `Money` obtiene un precio posible para el negocio,
 * fuerce o no la petición por la validación del contrato (el mismo dato puede
 * aparecer en las dos fronteras: `minimum` en el contrato y aquí la invariante).
 */
public class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    /** Construye un `Money` y valida la invariante antes de devolverlo. */
    public static Money of(BigDecimal candidate) {
        if (candidate == null) {
            throw new InvalidValueException("Money value is required");
        }
        // la invariante del precio: estrictamente mayor que cero
        if (candidate.signum() <= 0) {
            throw new InvalidValueException("Money value must be greater than zero");
        }
        // precisión monetaria: como mucho dos decimales
        if (candidate.stripTrailingZeros().scale() > 2) {
            throw new InvalidValueException("Money value must have at most two decimals");
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
