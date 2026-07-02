package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.ChequeAllocationType;

@Converter(autoApply = true)
public class ChequeAllocationTypeConverter implements AttributeConverter<ChequeAllocationType, String> {

    @Override
    public String convertToDatabaseColumn(ChequeAllocationType chequeAllocationType) {
        return chequeAllocationType == null ? null : chequeAllocationType.getLabel();
    }

    @Override
    public ChequeAllocationType convertToEntityAttribute(String label) {
        return Strings.isBlank(label) ? null : ChequeAllocationType.getByLabel(label);
    }
}
