package uk.gov.hmcts.opal.common.service;

import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptorV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;


public abstract class AbstractPermissionService {

    protected void checkPermission(UserStateV2 userState, PermissionDescriptorV2 permission) {
        if (permission == null || !userState.anyBusinessUnitUserHasPermission(permission)) {
            throw new PermissionNotAllowedException(permission);
        }
    }
}

