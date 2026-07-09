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
import uk.gov.hmcts.opal.exception.BusinessUnitUserNotFoundException;
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
        String ifMatch
    ) {
        log.debug(":addPaymentCardRequest:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        short buId = Short.parseShort(businessUnitId);
        String businessUnitUserId = getBusinessUnitUserIdForBusinessUnit(userState, buId);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.AMEND_PAYMENT_TERMS)) {
            return defendantAccountPaymentTermsServiceProxy.addPaymentCardRequest(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                ifMatch
            );
        } else {
            throw new PermissionNotAllowedException(FinesPermission.AMEND_PAYMENT_TERMS);
        }
    }

    private String getBusinessUnitUserIdForBusinessUnit(UserState userState, short businessUnitId) {
        return userState.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElseThrow(() -> new BusinessUnitUserNotFoundException(businessUnitId));
    }
}
