package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.document.DocumentEntityStatus;

@Converter(autoApply = true)
public class DocumentEntityStatusConverter implements AttributeConverter<DocumentEntityStatus, String> {

    @Override
    public String convertToDatabaseColumn(DocumentEntityStatus status) {
        if (status == null) {
            return null;
        }
        return status.getLabel();
    }

    @Override
    public DocumentEntityStatus convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return DocumentEntityStatus.getByLabel(label);
    }
}
