package uk.gov.hmcts.opal.actions.defendantaccount;

import io.restassured.response.Response;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/** Encapsulates payment-term API calls for the account created by the current scenario. */
public class PaymentTermsActions extends BaseStepDef {

    public Response addPaymentTerms(String businessUnitId, String etag, JSONObject request) {
        return authorisedJsonRequest()
            .header("Business-Unit-Id", businessUnitId)
            .header("If-Match", etag)
            .body(request.toString())
            .post(accountUrl() + "/payment-terms");
    }

    public Response getLatestPaymentTerms() {
        return authorisedJsonRequest().get(accountUrl() + "/payment-terms/latest");
    }

    private String accountUrl() {
        return getTestUrl() + "/defendant-accounts/" + scenarioContext().getCreatedDefendantAccountIdOrFail();
    }
}
