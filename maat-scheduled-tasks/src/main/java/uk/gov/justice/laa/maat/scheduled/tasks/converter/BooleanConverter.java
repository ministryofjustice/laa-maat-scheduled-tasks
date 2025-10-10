package uk.gov.justice.laa.maat.scheduled.tasks.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    private static final String STRING_Y = "Y";
    private static final String STRING_N = "N";

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (Boolean.TRUE.equals(attribute)) {
            return STRING_Y;
        } else if (Boolean.FALSE.equals(attribute)) {
            return STRING_N;
        }

        return null;
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            return dbData.equals(STRING_Y);
        }
        
        return false;
    }

}
