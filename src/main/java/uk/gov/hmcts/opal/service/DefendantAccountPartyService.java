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
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
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

    public GetDefendantAccountPartyResponse addDefendantAccountParty(
        Long defendantAccountId, String authHeaderValue, String ifMatch,
        String businessUnitId, AddDefendantAccountPartyRequest request) {

        log.debug(":addDefendantAccountParty: buId: {},  request: \n{}", businessUnitId, request);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        short buId = Short.parseShort(businessUnitId);

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(buId,
                                                        FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountPartyServiceProxy.addDefendantAccountParty(defendantAccountId,
                                                               businessUnitId,
                                                               getBusinessUnitUserIdForBusinessUnit(userState, buId),
                                                               postedBy,
                                                               userState.getUserName(),
                                                               ifMatch,
                                                               request);
        } else {
            throw new PermissionNotAllowedException(buId, FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }


    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(
        Long defendantAccountId, Long defendantAccountPartyId, String authHeaderValue, String ifMatch,
        String businessUnitId, DefendantAccountParty request) {

        log.debug(":replaceDefendantAccountParty: buId: {},  request: \n{}", businessUnitId, request.toPrettyJson());

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        short buId = Short.parseShort(businessUnitId);

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(buId,
                FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountPartyServiceProxy.replaceDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, request, ifMatch, businessUnitId, postedBy, userState.getUserName(),
                getBusinessUnitUserIdForBusinessUnit(userState, buId));
        } else {
            throw new PermissionNotAllowedException(buId, FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }

    public RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String ifMatch, String authHeaderValue,
        RemoveDefendantAccountPartyRequest request) {

        log.debug(":removeDefendantAccountParty: buId: {},  request: \n{}", businessUnitId, request.toPrettyJson());

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(businessUnitId,
            FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountPartyServiceProxy.removeDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, businessUnitId,
                getBusinessUnitUserIdForBusinessUnit(userState, businessUnitId), postedBy, userState.getUserName(),
                ifMatch, request);
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
