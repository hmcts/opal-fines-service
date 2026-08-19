package uk.gov.hmcts.opal.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.gov.hmcts.opal.validation.JsonNullableRequiredValidator;

@Documented
@Constraint(validatedBy = JsonNullableRequiredValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonNullableRequired {

    String message() default "must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
