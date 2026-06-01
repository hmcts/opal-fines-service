package uk.gov.hmcts.opal.common.service;

import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;


public abstract class AbstractPermissionService {

    protected void checkPermission(UserState userState, PermissionDescriptor permission) {
        if (permission == null || !userState.anyBusinessUnitUserHasPermission(permission)) {
            throw new PermissionNotAllowedException(permission);
        }
    }
}

