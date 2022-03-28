package io.github.crudzilla.persistency.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class IntToBooleanConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }

        return attribute ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return  null;
        }

        return dbData == 0 ? Boolean.FALSE : Boolean.TRUE;
    }
}