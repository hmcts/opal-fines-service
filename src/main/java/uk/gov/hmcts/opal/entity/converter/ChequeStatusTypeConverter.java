package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.ChequeStatusType;

@Converter(autoApply = true)
public class ChequeStatusTypeConverter implements AttributeConverter<ChequeStatusType, String> {

    @Override
    public String convertToDatabaseColumn(ChequeStatusType chequeStatusType) {
        return chequeStatusType == null ? null : chequeStatusType.getLabel();
    }

    @Override
    public ChequeStatusType convertToEntityAttribute(String label) {
        return Strings.isBlank(label) ? null : ChequeStatusType.getByLabel(label);
    }
}
