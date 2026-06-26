package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import uk.gov.hmcts.opal.entity.TransactionType;

public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType transactionType) {
        if (transactionType == null) {
            return null;
        }
        return transactionType.getLabel();
    }

    @Override
    public TransactionType convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return TransactionType.getByLabel(label);
    }
}
