package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.SignatureSource;

@Converter(autoApply = true)
public class SignatureSourceConverter implements AttributeConverter<SignatureSource, String> {

    @Override
    public String convertToDatabaseColumn(SignatureSource type) {
        if (type == null) {
            return null;
        }
        return type.getLabel();
    }

    @Override
    public SignatureSource convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return SignatureSource.getByLabel(label);
    }
}
