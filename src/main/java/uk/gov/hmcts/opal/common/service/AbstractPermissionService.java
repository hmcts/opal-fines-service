package uk.gov.hmcts.opal.common.service;

import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;

/**
 * Base class for services that require permission checking.
 * Provides a reusable method to check if a user has the required permission.
 *
 * <p>This class is intentionally kept in the common package so it can be
 * moved to {@code opal-common-lib} in a future release with no structural changes.
 */
public abstract class AbstractPermissionService {

    protected void checkPermission(UserState userState, PermissionDescriptor permission) {
        if (permission == null) {
            return;
        }
        if (!userState.anyBusinessUnitUserHasPermission(permission)) {
            throw new PermissionNotAllowedException(permission);
        }
    }
}

