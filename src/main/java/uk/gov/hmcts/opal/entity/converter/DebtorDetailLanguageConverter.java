package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.debtordetail.Language;

import java.util.Objects;

@Converter(autoApply = true)
public class DebtorDetailLanguageConverter implements AttributeConverter<Language, String> {

    @Override
    public String convertToDatabaseColumn(Language attribute) {
        return Objects.isNull(attribute) ? null : attribute.getCode();
    }

    @Override
    public Language convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : Language.fromCode(dbData);
    }
}
