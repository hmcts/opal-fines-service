package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;

@Converter(autoApply = true)
public class InstalmentPeriodConverter implements AttributeConverter<InstalmentPeriod, String> {

    @Override
    public String convertToDatabaseColumn(InstalmentPeriod instalmentPeriod) {
        return instalmentPeriod == null ? null : instalmentPeriod.getCode();
    }

    @Override
    public InstalmentPeriod convertToEntityAttribute(String code) {
        return Strings.isBlank(code) ? null : InstalmentPeriod.fromCode(code);
    }
}
