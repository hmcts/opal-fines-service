package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.AddPaymentTermsRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetPaymentTermsResponseDefendantAccount;

public interface DefendantAccountPaymentTermsServiceInterface {

    DefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId, Short businessUnitId,
        String businessUnitUserId, String postedByName, String ifMatch);

    GetPaymentTermsResponseDefendantAccount addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddPaymentTermsRequestDefendantAccount paymentTermsRequest);
}
