package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPartyServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountPartyService")
@RequiredArgsConstructor
public class DefendantAccountPartyService {

    private final DefendantAccountPartyServiceProxy defendantAccountPartyServiceProxy;

    private final UserStateService userStateService;


    public GetDefendantAccountPartyResponse getDefendantAccountParty(
        Long defendantAccountId,
        Long defendantAccountPartyId,
        String authHeaderValue) {

        log.debug(":getDefendantAccountParty:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountPartyServiceProxy.getDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        String authHeaderValue, String ifMatch, String businessUnitId, DefendantAccountParty request) {
        log.debug(":replaceDefendantAccountParty");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        short buId = Short.parseShort(businessUnitId);

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(buId,
                FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountPartyServiceProxy.replaceDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, request, ifMatch, businessUnitId, postedBy,
                getBusinessUnitUserIdForBusinessUnit(userState, buId));
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }

    private String getBusinessUnitUserIdForBusinessUnit(UserState userState, short buId) {
        return userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse("");
    }
}
