package uk.gov.hmcts.opal.authorisation.aspect;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.authorisation.model.Permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionNotAllowedExceptionTest {

    @Test
    void constructor_ShouldSetPermission() {
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        PermissionNotAllowedException exception = new PermissionNotAllowedException(permission);

        assertEquals(permission, exception.getPermission());
    }

    @Test
    void constructor_ShouldSetMessage() {
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        PermissionNotAllowedException exception = new PermissionNotAllowedException(permission);

        assertEquals(permission + " permission is not allowed for the user", exception.getMessage());
    }
}
