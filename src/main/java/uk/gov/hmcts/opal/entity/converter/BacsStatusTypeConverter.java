package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.BacsStatusType;

@Converter(autoApply = true)
public class BacsStatusTypeConverter implements AttributeConverter<BacsStatusType, String> {

    @Override
    public String convertToDatabaseColumn(BacsStatusType bacsStatusType) {
        if (bacsStatusType == null) {
            return null;
        }
        return bacsStatusType.getLabel();
    }

    @Override
    public BacsStatusType convertToEntityAttribute(String label) {
        if (label == null) {
            return null;
        }
        return BacsStatusType.getByLabel(label);
    }
}
