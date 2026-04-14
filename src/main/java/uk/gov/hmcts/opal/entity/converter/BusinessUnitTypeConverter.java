package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitType;

import java.util.Objects;

@Converter(autoApply = true)
public class BusinessUnitTypeConverter implements AttributeConverter<BusinessUnitType, String> {

    @Override
    public String convertToDatabaseColumn(BusinessUnitType type) {
        return !Objects.isNull(type) ? type.getLabel() : null;
    }

    @Override
    public BusinessUnitType convertToEntityAttribute(String label) {
        return Strings.isNotBlank(label) ? BusinessUnitType.getByLabel(label) : null;
    }
}
