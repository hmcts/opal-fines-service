package uk.gov.hmcts.opal.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.annotation.JsonNullableRequired;

public class JsonNullableRequiredValidator implements ConstraintValidator<JsonNullableRequired, JsonNullable<?>> {

    @Override
    public boolean isValid(JsonNullable<?> value, ConstraintValidatorContext context) {
        return value != null && value.isPresent();
    }
}
