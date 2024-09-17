package uk.gov.hmcts.opal.controllers;

import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Set;

public class UserStateBuilder {

    public static UserState createUserState() {
        return createUserState(Set.of(
            createRole(Set.of(
                createPermission(
                    Permissions.ACCOUNT_ENQUIRY_NOTES.id,
                    Permissions.ACCOUNT_ENQUIRY_NOTES.description
                ),
                createPermission(
                    Permissions.ACCOUNT_ENQUIRY.id,
                    Permissions.ACCOUNT_ENQUIRY.description
                )
            ))));
    }

    public static UserState createUserState(Set<BusinessUnitUserPermissions> roles) {
        return UserState.builder()
            .userId(345L)
            .userName("John Smith")
            .roles(roles)
            .build();
    }

    public static BusinessUnitUserPermissions createRole(Set<Permission> permissions) {
        return BusinessUnitUserPermissions.builder()
            .businessUserId("JK0320")
            .businessUnitId((short)50)
            .permissions(permissions)
            .build();
    }

    public static Set<Permission> createSinglePermissions(long id) {
        return Set.of(createPermission(id, "any desc"));
    }

    public static Permission createPermission(long id, String desc) {
        return Permission.builder()
                          .permissionId(id)
                          .permissionName("Do Stuff")
                          .build();
    }
}
