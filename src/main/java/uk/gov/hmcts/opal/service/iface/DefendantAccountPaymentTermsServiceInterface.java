package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsRequestDefendantAccount;

public interface DefendantAccountPaymentTermsServiceInterface {

    GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String postedByName, String ifMatch);

    DefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        DefendantAccountPaymentTermsRequestDefendantAccount paymentTermsRequest);
}
