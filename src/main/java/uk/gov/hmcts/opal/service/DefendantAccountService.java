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
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountServiceProxy defendantAccountServiceProxy;

    private final UserStateService userStateService;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId, String authHeaderValue) {
        log.debug(":getHeaderSummary:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getHeaderSummary(defendantAccountId);
    }

    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto,
                                                                    String authHeaderValue) {
        log.debug(":searchDefendantAccounts:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.searchDefendantAccounts(accountSearchDto);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPartyResponse getDefendantAccountParty(
        Long defendantAccountId,
        Long defendantAccountPartyId,
        String authHeaderValue) {

        log.debug(":getDefendantAccountParty:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId, String authHeaderValue) {

        log.debug(":getPaymentTerms:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getPaymentTerms(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId, String authHeaderValue) {
        log.debug(":getAtAGlance");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getAtAGlance(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(
        Long defendantAccountId,
        String authHeaderValue) {

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getDefendantAccountFixedPenalty(defendantAccountId);
    }

    public DefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                           String businessUnitId,
                                                           UpdateDefendantAccountRequest request,
                                                           String ifMatch,
                                                           String authHeaderValue) {
        log.debug(":updateDefendantAccount:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.ACCOUNT_MAINTENANCE)) {
            short buId = Short.parseShort(businessUnitId);

            String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
                .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
                .filter(id -> !id.isBlank())
                .orElse(userState.getUserName());

            return defendantAccountServiceProxy.updateDefendantAccount(
                defendantAccountId, businessUnitId, request, ifMatch, postedBy
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ACCOUNT_MAINTENANCE);
        }
    }

    public EnforcementStatus getEnforcementStatus(Long defendantAccountId, String authHeaderValue) {

        log.debug(":getEnforcementStatus:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getEnforcementStatus(defendantAccountId);
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
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(buId,
                FinesPermission.ACCOUNT_MAINTENANCE)) {
            return defendantAccountServiceProxy.replaceDefendantAccountParty(defendantAccountId,
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


    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeaderValue
    ) {
        log.debug(":addPaymentCardRequest:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountServiceProxy.addPaymentCardRequest(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                ifMatch,
                authHeaderValue
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.AMEND_PAYMENT_TERMS);
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
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(null);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)) {

            return defendantAccountServiceProxy.addEnforcement(
                defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, authHeaderValue, request
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT);
        }
    }

    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String ifMatch,
        String authHeaderValue,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTerms:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        short buId = Short.parseShort(businessUnitId);
        String postedBy = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        if (userState.hasBusinessUnitUserWithPermission(buId,
            FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountServiceProxy.addPaymentTerms(defendantAccountId,
                businessUnitId,
                ifMatch,
                authHeaderValue,
                addPaymentTermsRequest);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }
}
