package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountPaymentTermsServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountPaymentTermsService")
@RequiredArgsConstructor
public class DefendantAccountPaymentTermsService {

    private final DefendantAccountPaymentTermsServiceProxy defendantAccountPaymentTermsServiceProxy;

    private final UserStateService userStateService;


    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        log.debug(":getPaymentTerms:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountPaymentTermsServiceProxy.getPaymentTerms(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch
    ) {
        log.debug(":addPaymentCardRequest:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)) {
            String derivedBusinessUnitUserId = userState.getBusinessUnitUserForBusinessUnit(
                    Short.parseShort(businessUnitId))
                .map(BusinessUnitUser::getBusinessUnitUserId)
                .filter(id -> !id.isBlank())
                .orElse(userState.getUserName());
            String postedByName = userState.getUserName();

            return defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
                defendantAccountId,
                businessUnitId,
                derivedBusinessUnitUserId,
                postedByName,
                ifMatch
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.AMEND_PAYMENT_TERMS);
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
