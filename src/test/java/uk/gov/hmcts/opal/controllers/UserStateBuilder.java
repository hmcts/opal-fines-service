package uk.gov.hmcts.opal.controllers;

import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Set;

public class UserStateBuilder {

    public static UserState createUserState() {
        return createUserState(Set.of(
            createBusinessUnitUser(Set.of(
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

    public static UserState createUserState(Set<BusinessUnitUser> businessUnitUser) {
        return UserState.builder()
            .userId(345L)
            .userName("John Smith")
            .businessUnitUser(businessUnitUser)
            .build();
    }

    public static BusinessUnitUser createBusinessUnitUser(Set<Permission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId("JK0320")
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
