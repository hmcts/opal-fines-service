package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;

import java.util.Objects;

@Converter(autoApply = true)
public class DefendantTransactionTypeConverter implements AttributeConverter<DefendantTransactionType, String> {

    @Override
    public String convertToDatabaseColumn(DefendantTransactionType attribute) {
        return Objects.isNull(attribute) ? null : attribute.getLabel();
    }

    @Override
    public DefendantTransactionType convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : DefendantTransactionType.getByLabel(dbData);
    }
}
