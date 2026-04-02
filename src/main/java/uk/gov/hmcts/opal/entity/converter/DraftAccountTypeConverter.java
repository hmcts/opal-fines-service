package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

@Converter
public class DraftAccountTypeConverter implements AttributeConverter<DraftAccountType, String> {

    @Override
    public String convertToDatabaseColumn(DraftAccountType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public DraftAccountType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DraftAccountType.getByLabel(dbData);
    }
}
