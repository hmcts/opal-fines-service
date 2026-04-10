package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountType;

@Converter(autoApply = true)
public class ConsolidatedAccountTypeConverter implements AttributeConverter<ConsolidatedAccountType, String> {

    @Override
    public String convertToDatabaseColumn(ConsolidatedAccountType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public ConsolidatedAccountType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ConsolidatedAccountType.getByLabel(dbData);
    }
}
