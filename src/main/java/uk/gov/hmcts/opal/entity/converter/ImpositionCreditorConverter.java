package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;

@Converter(autoApply = true)
public class ImpositionCreditorConverter implements AttributeConverter<ImpositionCreditor, String> {

    @Override
    public String convertToDatabaseColumn(ImpositionCreditor attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public ImpositionCreditor convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : ImpositionCreditor.getByLabel(dbData);
    }
}
