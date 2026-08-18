package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountType;

@Converter(autoApply = true)
public class EnforcementAccountTypeConverter implements AttributeConverter<EnforcementAccountType, String> {

    @Override
    public String convertToDatabaseColumn(EnforcementAccountType enforcementAccountType) {
        if (enforcementAccountType == null) {
            return null;
        }
        return enforcementAccountType.getCode();
    }

    @Override
    public EnforcementAccountType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return EnforcementAccountType.getByCode(code);
    }
}
