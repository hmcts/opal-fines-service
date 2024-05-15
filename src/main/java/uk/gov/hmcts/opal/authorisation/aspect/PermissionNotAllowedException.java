package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.Getter;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.Role;

@Getter
public class PermissionNotAllowedException extends RuntimeException {

    private final Permissions permission;
    private final Role role;

    public PermissionNotAllowedException(Permissions value) {
        super(value + " permission is not allowed for the user");
        this.permission = value;
        this.role = null;
    }

    public PermissionNotAllowedException(Permissions permission, Role role) {
        super(permission + " permission is not allowed for the role " + role);
        this.permission = permission;
        this.role = role;
    }
}
