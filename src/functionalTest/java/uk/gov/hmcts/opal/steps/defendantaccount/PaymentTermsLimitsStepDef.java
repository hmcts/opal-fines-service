package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import uk.gov.hmcts.opal.workflows.defendantaccount.PaymentTermsWorkflow;

/** Binds payment-term amendment scenarios to their workflow. */
public class PaymentTermsLimitsStepDef {
    private final PaymentTermsWorkflow workflow = new PaymentTermsWorkflow();

    @Given("a payment-term amendment fixture with a 250-character reason and the following values")
    public void prepareFixture(DataTable table) throws JSONException {
        workflow.prepareMaximumLengthFixture(table.asMap(String.class, String.class));
    }

    @When("I submit the maximum-length payment-term amendment for business unit {string}")
    public void submitAmendment(String businessUnitId) throws JSONException {
        workflow.submitAmendment(businessUnitId);
    }

    @Then("the latest payment terms match the submitted amendment including the reason content and length")
    public void verifyLatestTerms() throws JSONException {
        workflow.assertLatestTermsMatch();
    }
}
