package uk.gov.hmcts.opal.workflows.defendantaccount;

import io.restassured.response.Response;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.actions.defendantaccount.PaymentTermsActions;
import uk.gov.hmcts.opal.actions.defendantaccount.PaymentTermsRequestFactory;
import uk.gov.hmcts.opal.assertions.defendantaccount.PaymentTermsAssertions;

/** Coordinates maximum-length payment-term amendment scenarios and their submitted fixture. */
public class PaymentTermsWorkflow {
    private final PaymentTermsRequestFactory requestFactory = new PaymentTermsRequestFactory();
    private final PaymentTermsActions actions = new PaymentTermsActions();
    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();
    private final PaymentTermsAssertions assertions = new PaymentTermsAssertions();
    private JSONObject paymentTerms;

    public void prepareMaximumLengthFixture(Map<String, String> values) throws JSONException {
        paymentTerms = requestFactory.buildMaximumLengthTerms(values);
        assertions.assertMaximumLengthFixture(paymentTerms);
    }

    public void submitAmendment(String businessUnitId) throws JSONException {
        Response status = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertions.assertAccountVersion(status);
        // Preserve the complete Legacy version by forwarding the account ETag unchanged.
        Response response = actions.addPaymentTerms(businessUnitId, status.getHeader("ETag"),
            requestFactory.buildAmendment(paymentTerms));
        assertions.assertAmendmentSucceeded(response);
    }

    public void assertLatestTermsMatch() throws JSONException {
        assertions.assertLatestTermsMatch(actions.getLatestPaymentTerms(), paymentTerms);
    }
}
