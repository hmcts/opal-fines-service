package uk.gov.hmcts.opal.authorisation.aspect;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionNotAllowedExceptionTest {

    @Test
    void constructor_ShouldSetPermission() {
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        PermissionNotAllowedException exception = new PermissionNotAllowedException(permission);

        assertEquals(permission, exception.getPermission()[0]);
    }

    @Test
    void constructor_ShouldSetMessage() {
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        PermissionNotAllowedException exception = new PermissionNotAllowedException(permission);

        assertEquals("[" + permission + "]" + " permission(s) are not enabled for the user.",
                     exception.getMessage());
    }

    @Test
    void constructor2_ShouldSetMessage() {
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        PermissionNotAllowedException exception = new PermissionNotAllowedException(
            permission, BusinessUnitUser.builder().businessUnitUserId("A001").build());

        assertEquals(permission + " permission is not enabled for the business unit user: A001",
                     exception.getMessage());
    }
}
