package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@Service
@Slf4j(topic = "opal.MinorCreditorService")
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

    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String authHeaderValue) {
        log.debug(":updateMinorCreditorAccount:");

        if (etag == null) {
            throw new ResourceConflictException(
                "CreditorAccount", minorCreditorId, "ETag header is required", null);
        }

        if (request == null || request.getPayment() == null || request.getPayment().getHoldPayment() == null) {
            throw new IllegalArgumentException("Payment group must be provided");
        }

        CreditorAccountEntity.Lite account = creditorAccountRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException("Minor creditor account not found: " + minorCreditorId));

        if (account.getCreditorAccountType() == null || !account.getCreditorAccountType().isMinorCreditor()) {
            throw new EntityNotFoundException("Minor creditor account not found: " + minorCreditorId);
        }

        Short businessUnitId = account.getBusinessUnitId();
        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);
        if (businessUnitId == null || !userState.hasBusinessUnitUserWithPermission(businessUnitId,
            FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD)) {
            throw new PermissionNotAllowedException(FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD);
        }

        String postedBy = userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());

        return minorCreditorSearchProxy.updateMinorCreditorAccount(minorCreditorId, request, etag, postedBy);
    }

}
