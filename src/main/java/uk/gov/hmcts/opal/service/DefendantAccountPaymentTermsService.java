package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountPaymentTermsService")
@RequiredArgsConstructor
public class DefendantAccountPaymentTermsService {

    private final DefendantAccountPaymentTermsServiceProxy defendantAccountPaymentTermsServiceProxy;

    private final UserStateService userStateService;

    private final BusinessUnitService businessUnitService;

    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        log.debug(":getPaymentTerms:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountPaymentTermsServiceProxy.getPaymentTerms(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    // Using V2 FINES-domain user state for the payment-card request path.
    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        Short businessUnitId,
        String ifMatch
    ) {
        log.debug(":addPaymentCardRequest:");

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        DomainBusinessUnitUsers businessUnitUsers = userState.getDomainBusinessUnitUsers(Domain.FINES);

        if (businessUnitService.hasBusinessUnitUserWithPermission(
            businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS)) {
            String businessUnitUserId = businessUnitService.getBusinessUnitUserIdForBusinessUnit(
                businessUnitUsers, businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
            String postedByName = userState.getUsername();

            return defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                postedByName,
                ifMatch
            );
        } else {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }

    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTerms:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        short buId = Short.parseShort(businessUnitId);
        String businessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(buId)
            .map(uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(userState.getUserName());
        String postedByName = userState.getUserName();

        if (addPaymentTermsRequest != null && addPaymentTermsRequest.getPaymentTerms() != null) {
            addPaymentTermsRequest.getPaymentTerms().setPostedDetails(PostedDetails.builder()
                .postedBy(businessUnitUserId)
                .postedByName(postedByName)
                .build());
        }

        if (userState.hasBusinessUnitUserWithPermission(buId,
            FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountPaymentTermsServiceProxy.addPaymentTerms(defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                postedByName,
                ifMatch,
                addPaymentTermsRequest);
        } else {
            throw new PermissionNotAllowedException(buId, FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }
}
