package es.cesguiro.shop.shared.domain;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte Money <-> columna DECIMAL(8,2).
 */
@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money == null ? null : money.amount();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal amount) {
        return amount == null ? null : Money.of(amount);
    }
}
