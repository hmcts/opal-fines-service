package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.PartyAccountType;

@Converter(autoApply = true)
public class PartyAccountTypeConverter implements AttributeConverter<PartyAccountType, String> {

    @Override
    public String convertToDatabaseColumn(PartyAccountType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public PartyAccountType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return PartyAccountType.getByLabel(dbData);
    }
}
