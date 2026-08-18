package uk.gov.hmcts.opal.exception;

import lombok.Getter;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;

@Getter
public class RequiredPermissionException extends RuntimeException {

    private final FinesPermission permission;

    public RequiredPermissionException(FinesPermission permission) {
        super("User requires permission: " + permission.getDescription());
        this.permission = permission;
    }
}
