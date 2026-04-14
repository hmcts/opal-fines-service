package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;

@Converter(autoApply = true)
public class DefendantAccountStatusConverter implements AttributeConverter<DefendantAccountStatus, String> {

    @Override
    public String convertToDatabaseColumn(DefendantAccountStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public DefendantAccountStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DefendantAccountStatus.getByLabel(dbData);
    }
}
