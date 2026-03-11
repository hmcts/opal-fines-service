package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitType;

@Converter(autoApply = true)
public class BusinessUnitTypeConverter implements AttributeConverter<BusinessUnitType, String> {

    @Override
    public String convertToDatabaseColumn(BusinessUnitType type) {
        return type.getLabel();
    }

    @Override
    public BusinessUnitType convertToEntityAttribute(String label) {
        return BusinessUnitType.getByLabel(label);
    }
}
