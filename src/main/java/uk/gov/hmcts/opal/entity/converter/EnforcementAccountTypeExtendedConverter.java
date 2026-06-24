package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeExtended;

@Converter(autoApply = true)
public class EnforcementAccountTypeExtendedConverter
    implements AttributeConverter<EnforcementAccountTypeExtended, String> {

    @Override
    public String convertToDatabaseColumn(EnforcementAccountTypeExtended enforcementAccountType) {
        if (enforcementAccountType == null) {
            return null;
        }
        return enforcementAccountType.getCode();
    }

    @Override
    public EnforcementAccountTypeExtended convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return EnforcementAccountTypeExtended.fromCode(code);
    }
}
