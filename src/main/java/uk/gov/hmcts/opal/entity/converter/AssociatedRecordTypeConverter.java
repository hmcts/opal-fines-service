package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;

@Converter(autoApply = true)
public class AssociatedRecordTypeConverter implements AttributeConverter<AssociatedRecordType, String> {

    @Override
    public String convertToDatabaseColumn(AssociatedRecordType type) {
        if (type == null) {
            return null;
        }
        return type.getLabel();
    }

    @Override
    public AssociatedRecordType convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return AssociatedRecordType.getByLabel(label);
    }
}
