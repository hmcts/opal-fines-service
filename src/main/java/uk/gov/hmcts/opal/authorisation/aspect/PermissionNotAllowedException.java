package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.Getter;
import uk.gov.hmcts.opal.authorisation.model.Permissions;

@Getter
public class PermissionNotAllowedException extends Throwable {

    private final Permissions permission;

    public PermissionNotAllowedException(Permissions value) {
        super(value + " permission is not allowed for the user");
        this.permission = value;
    }
}
