package es.cesguiro.shop.shared.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte Stock <-> columna INT.
 */
@Converter
public class StockConverter implements AttributeConverter<Stock, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Stock stock) {
        return stock == null ? null : stock.units();
    }

    @Override
    public Stock convertToEntityAttribute(Integer units) {
        return units == null ? null : Stock.of(units);
    }
}
