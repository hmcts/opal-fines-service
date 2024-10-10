package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.Getter;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;

import java.util.Arrays;
import java.util.Collection;

@Getter
public class PermissionNotAllowedException extends RuntimeException {

    private final Permissions[] permission;
    private final BusinessUnitUser businessUnitUser;

    public PermissionNotAllowedException(Permissions... value) {
        super(Arrays.toString(value) + " permission(s) are not allowed for the user.");
        this.permission = value;
        this.businessUnitUser = null;
    }

    public PermissionNotAllowedException(Collection<Short> buIds, Permissions... value) {
        super(Arrays.toString(value) + " permission(s) are not allowed for the user in business units: " + buIds);
        this.permission = value;
        this.businessUnitUser = null;
    }

    public PermissionNotAllowedException(Permissions permission,
                                         BusinessUnitUser businessUnitUser) {
        super(permission + " permission is not allowed for the business unit user: "
                  + businessUnitUser.getBusinessUnitUserId());
        this.permission = new Permissions[] {permission};
        this.businessUnitUser = businessUnitUser;
    }
}
