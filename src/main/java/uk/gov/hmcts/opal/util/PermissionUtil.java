package uk.gov.hmcts.opal.util;

import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.BusinessUnitRef;
import uk.gov.hmcts.opal.service.UserStateService;

import java.util.List;
import java.util.Optional;

public class PermissionUtil {

    public static BusinessUnitUser getRequiredBusinessUnitUser(UserState userState, Short businessUnitId) {
        return userState.getBusinessUnitUserForBusinessUnit(businessUnitId).orElseThrow(() -> new
            AccessDeniedException("User does not have assigned permissions in business unit: " + businessUnitId));
    }

    public static boolean checkBusinessUnitUserHasPermission(BusinessUnitUser businessUnitUser,
                                                             FinesPermission permission) {
        if (businessUnitUser.doesNotHavePermission(permission)) {
            throw new AccessDeniedException(
                "User does not have the required permission: "
                    + permission.getDescription());
        }
        return true;
    }

    public static boolean checkAnyBusinessUnitUserHasPermission(UserState userState, FinesPermission permission) {
        if (userState.noBusinessUnitUserHasPermission(permission)) {
            throw new AccessDeniedException(
                "User does not have the required permission: "
                    + permission.getDescription());
        }
        return true;
    }

    public static  <B extends BusinessUnitRef> List<B> filterBusinessUnitsByPermission(
        UserStateService userStateService,
        List<B> refData,
        Optional<FinesPermission> optPermission,
        String authHeaderValue) {

        return optPermission.map(
            permission -> {
                UserState.UserBusinessUnits userBusinessUnits = userStateService
                    .checkForAuthorisedUser(authHeaderValue)
                    .allBusinessUnitUsersWithPermission(permission);
                return refData
                    .stream()
                    .filter(bu -> userBusinessUnits
                        .containsBusinessUnit(bu.getBusinessUnitId()))
                    .toList();
            }).orElse(refData);
    }
}
