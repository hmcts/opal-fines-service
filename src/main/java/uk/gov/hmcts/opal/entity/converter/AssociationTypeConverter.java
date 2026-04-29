package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;

import java.util.Objects;

@Converter(autoApply = true)
public class AssociationTypeConverter implements AttributeConverter<AssociationType, String> {

    @Override
    public String convertToDatabaseColumn(AssociationType type) {
        return Objects.isNull(type) ? null : type.getLabel();
    }

    @Override
    public AssociationType convertToEntityAttribute(String label) {
        return Strings.isBlank(label) ? null : AssociationType.getByLabel(label);
    }
}
