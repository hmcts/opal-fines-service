package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.Priority;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, String> {

    @Override
    public String convertToDatabaseColumn(Priority type) {
        if (type == null) {
            return null;
        }
        return type.getLabel();
    }

    @Override
    public Priority convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return Priority.getByLabel(label);
    }
}
