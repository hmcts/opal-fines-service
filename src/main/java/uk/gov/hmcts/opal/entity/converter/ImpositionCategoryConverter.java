package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.result.ImpositionCategory;

@Converter(autoApply = true)
public class ImpositionCategoryConverter implements AttributeConverter<ImpositionCategory, String> {

    @Override
    public String convertToDatabaseColumn(ImpositionCategory attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public ImpositionCategory convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : ImpositionCategory.getByLabel(dbData);
    }
}
