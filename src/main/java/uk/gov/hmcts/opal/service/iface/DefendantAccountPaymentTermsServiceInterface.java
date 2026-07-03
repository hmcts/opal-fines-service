package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;

public interface DefendantAccountPaymentTermsServiceInterface {

    GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId,
        String ifMatch);

    GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest);
}
