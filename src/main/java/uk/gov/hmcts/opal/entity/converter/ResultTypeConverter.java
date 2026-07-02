package uk.gov.hmcts.opal.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.opal.entity.result.ResultType;

@Converter(autoApply = true)
public class ResultTypeConverter implements AttributeConverter<ResultType, String> {

    @Override
    public String convertToDatabaseColumn(ResultType attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public ResultType convertToEntityAttribute(String dbData) {
        return Strings.isBlank(dbData) ? null : ResultType.getByLabel(dbData);
    }
}
