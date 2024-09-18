package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.Getter;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;

@Getter
public class PermissionNotAllowedException extends RuntimeException {

    private final Permissions permission;
    private final BusinessUnitUserPermissions businessUnitUserPermissions;

    public PermissionNotAllowedException(Permissions value) {
        super(value + " permission is not allowed for the user");
        this.permission = value;
        this.businessUnitUserPermissions = null;
    }

    public PermissionNotAllowedException(Permissions permission,
                                         BusinessUnitUserPermissions businessUnitUserPermissions) {
        super(permission + " permission is not allowed for the businessUnitUserPermissions "
                  + businessUnitUserPermissions);
        this.permission = permission;
        this.businessUnitUserPermissions = businessUnitUserPermissions;
    }
}
