package uk.gov.hmcts.opal.authorisation.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.gov.hmcts.opal.authorisation.model.Permissions;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizedAnyBusinessUnitUserHasPermission {
    Permissions value();
}
