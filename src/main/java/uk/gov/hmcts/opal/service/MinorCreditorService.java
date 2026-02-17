package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.UpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class MinorCreditorService {

    private final MinorCreditorSearchProxy minorCreditorSearchProxy;

    private final UserStateService userStateService;
    private final CreditorAccountRepository creditorAccountRepository;

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

    public MinorCreditorAccountResponse updateMinorCreditorAccount(Long minorCreditorId,
                                                                   UpdateMinorCreditorAccountRequest request,
                                                                   String ifMatch,
                                                                   String authHeaderValue) {
        log.debug(":updateMinorCreditorAccount:");

        if (ifMatch == null || ifMatch.isBlank()) {
            throw new uk.gov.hmcts.opal.exception.ResourceConflictException(
                "CreditorAccount", minorCreditorId, "If-Match header is required", null);
        }

        if (request == null || request.getPayoutHold() == null || request.getPayoutHold().getPayoutHold() == null) {
            throw new IllegalArgumentException("payout_hold group must be provided");
        }

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        CreditorAccountEntity.Lite account = creditorAccountRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException("Minor creditor account not found: " + minorCreditorId));

        if (account.getCreditorAccountType() == null || !account.getCreditorAccountType().isMinorCreditor()) {
            throw new EntityNotFoundException("Minor creditor account not found: " + minorCreditorId);
        }

        Short businessUnitId = account.getBusinessUnitId();
        if (businessUnitId == null || !userState.hasBusinessUnitUserWithPermission(businessUnitId,
            FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD)) {
            throw new PermissionNotAllowedException(FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        }

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        return minorCreditorSearchProxy.updateMinorCreditorAccount(minorCreditorId, request, ifMatch, postedBy);
    }

}
