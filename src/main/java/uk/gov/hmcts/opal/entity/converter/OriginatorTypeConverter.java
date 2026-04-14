package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;

@Converter(autoApply = true)
public class OriginatorTypeConverter implements AttributeConverter<OriginatorType, String> {

    @Override
    public String convertToDatabaseColumn(OriginatorType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public OriginatorType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return OriginatorType.getByLabel(dbData);
    }
}
