package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;

@Converter(autoApply = true)
public class DefendantTransactionStatusConverter implements AttributeConverter<DefendantTransactionStatus, String> {

    @Override
    public String convertToDatabaseColumn(DefendantTransactionStatus type) {
        if (type == null) {
            return null;
        }
        return type.getLabel();
    }

    @Override
    public DefendantTransactionStatus convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return DefendantTransactionStatus.getByLabel(label);
    }
}
