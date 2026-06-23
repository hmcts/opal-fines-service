package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.LowHighValue;

@Converter(autoApply = true)
public class LowHighConverter implements AttributeConverter<LowHighValue, String> {

    @Override
    public String convertToDatabaseColumn(LowHighValue value) {
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @Override
    public LowHighValue convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return LowHighValue.getByValue(value);
    }
}
