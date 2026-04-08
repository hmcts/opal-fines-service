package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;

@Converter(autoApply = true)
public class TermsTypeCodeConverter implements AttributeConverter<TermsTypeCode, String> {

    @Override
    public String convertToDatabaseColumn(TermsTypeCode termsTypeCode) {
        return termsTypeCode == null ? null : termsTypeCode.getCode();
    }

    @Override
    public TermsTypeCode convertToEntityAttribute(String code) {
        return Strings.isBlank(code) ? null : TermsTypeCode.fromCode(code);
    }
}
