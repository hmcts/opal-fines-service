package uk.gov.hmcts.opal.service;

import java.math.BigInteger;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@Service
@Slf4j(topic = "opal.MinorCreditorService")
@RequiredArgsConstructor
public class MinorCreditorService {

    private final MinorCreditorSearchProxy minorCreditorSearchProxy;

    private final UserStateService userStateService;

    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch entity,
                                                                        String authHeaderValue) {
        log.debug(":searchMinorCreditor:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.searchMinorCreditors(entity);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId) {
        log.debug(":getMinorCreditorAccount: id={}", minorCreditorAccountId);

        UserState userState = userStateService.checkForAuthorisedUser();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        MinorCreditorAccountResponse response =
            minorCreditorSearchProxy.getMinorCreditorAccount(minorCreditorAccountId);
        return redactBacsDetailsWhenNotPermitted(minorCreditorAccountId, response, userState);
    }

    public GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId,
        String authHeaderValue) {

        log.debug(":getMinorCreditorAccountAtAGlance: id= {}", minorCreditorId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.getMinorCreditorAtAGlance(minorCreditorId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId, String authHeaderValue) {
        log.debug(":getMinorCreditorAccount: id={}", minorCreditorAccountId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            MinorCreditorAccountResponse response = minorCreditorSearchProxy.getMinorCreditorAccount(
                minorCreditorAccountId);
            return redactBacsDetailsWhenNotPermitted(minorCreditorAccountId, response, userState);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetMinorCreditorAccountHeaderSummaryResponse getMinorCreditorAccountHeaderSummary(
        Long minorCreditorId,
        String authHeaderValue) {

        log.debug(":getMinorCreditorAccountHeaderSummary: id={}", minorCreditorId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.getHeaderSummary(minorCreditorId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorId,
        PatchMinorCreditorAccountRequest request,
        BigInteger ifMatch,
        String authHeaderValue,
        String businessUnitId) {
        log.debug(":updateMinorCreditorAccount:");

        if (ifMatch == null) {
            throw new ResourceConflictException(
                "CreditorAccount", minorCreditorId, "If-Match header is required", null);
        }

        if (request == null
            || request.getPayment() == null
            || request.getPayment().getHoldPayment() == null
            || request.getPartyDetails() == null
            || request.getAddress() == null) {
            throw new IllegalArgumentException("Payment, party_details and address groups must be provided");
        }

        Short businessUnitIdShort = businessUnitId != null ? Short.valueOf(businessUnitId) : null;

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (businessUnitId == null) {
            throw new PermissionNotAllowedException(
                businessUnitIdShort,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        }
        if (!userState.hasBusinessUnitUserWithPermission(businessUnitIdShort, FinesPermission.ACCOUNT_MAINTENANCE)) {
            throw new PermissionNotAllowedException(
                businessUnitIdShort,
                FinesPermission.ACCOUNT_MAINTENANCE);
        }
        if (!userState.hasBusinessUnitUserWithPermission(businessUnitIdShort,
            FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD)) {
            throw new PermissionNotAllowedException(
                businessUnitIdShort,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        }

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(businessUnitIdShort)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        return minorCreditorSearchProxy.updateMinorCreditorAccount(minorCreditorId, request, ifMatch, postedBy,
            businessUnitIdShort);
    }

    private MinorCreditorAccountResponse redactBacsDetailsWhenNotPermitted(
        Long minorCreditorAccountId,
        MinorCreditorAccountResponse response,
        UserState userState
    ) {
        if (response == null || response.getPayment() == null) {
            return response;
        }

        // Fail closed: if the account business unit cannot be confirmed here, redact BACS details.
        Short businessUnitId = response.getBusinessUnitId();
        boolean canViewBacs = businessUnitId != null
            && userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.VIEW_CREDITOR_BACS);

        log.debug(":redactBacsDetailsWhenNotPermitted: id={}, businessUnitId={}, canViewBacs={}",
            minorCreditorAccountId, businessUnitId, canViewBacs);

        if (canViewBacs) {
            return response;
        }

        response.getPayment().setPayByBacs(null);
        response.getPayment().setSortCode(null);
        response.getPayment().setAccountNumber(null);
        response.getPayment().setAccountName(null);
        response.getPayment().setAccountReference(null);
        return response;
    }
}
