package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountEnforcementServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountEnforcementService")
@RequiredArgsConstructor
public class DefendantAccountEnforcementService {

    private final DefendantAccountEnforcementServiceProxy defendantAccountEnforcementServiceProxy;

    private final UserStateService userStateService;

    public EnforcementStatus getEnforcementStatus(Long defendantAccountId, String authHeaderValue) {

        log.debug(":getEnforcementStatus:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountEnforcementServiceProxy.getEnforcementStatus(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public AddEnforcementResponse addEnforcement(Long defendantAccountId,
        String businessUnitId,
        String ifMatch,
        String authHeaderValue,
        AddDefendantAccountEnforcementRequest request) {

        log.debug(":addEnforcement:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(Short.parseShort(businessUnitId))
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(null);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)) {

            return defendantAccountEnforcementServiceProxy.addEnforcement(
                defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, authHeaderValue, request
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT);
        }
    }
}
