package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionWriteOffCode;

import java.util.Objects;

@Converter(autoApply = true)
public class DefendantTransactionWriteOffCodeConverter
    implements AttributeConverter<DefendantTransactionWriteOffCode, String> {

    @Override
    public String convertToDatabaseColumn(DefendantTransactionWriteOffCode attribute) {
        return Objects.isNull(attribute) ? null : attribute.getLabel();
    }

    @Override
    public DefendantTransactionWriteOffCode convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : DefendantTransactionWriteOffCode.getByLabel(dbData);
    }
}
