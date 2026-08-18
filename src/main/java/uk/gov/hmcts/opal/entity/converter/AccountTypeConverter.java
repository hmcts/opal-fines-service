package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public AccountType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return AccountType.getByCode(code);
    }
}
