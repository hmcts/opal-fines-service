package uk.gov.hmcts.opal.entity.defendant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DefendantAccountTypeConverter implements AttributeConverter<DefendantAccountType, String> {

    @Override
    public String convertToDatabaseColumn(DefendantAccountType type) {
        return type.getLabel();
    }

    @Override
    public DefendantAccountType convertToEntityAttribute(String label) {
        return DefendantAccountType.getByLabel(label);
    }
}
