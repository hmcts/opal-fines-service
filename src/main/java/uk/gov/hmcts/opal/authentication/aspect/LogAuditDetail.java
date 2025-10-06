package uk.gov.hmcts.opal.authentication.aspect;

import uk.gov.hmcts.opal.authorisation.model.LogActions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAuditDetail {

    LogActions action();

    boolean logJsonRequest() default false;

    String defaultJsonRequest() default "{}";

}
