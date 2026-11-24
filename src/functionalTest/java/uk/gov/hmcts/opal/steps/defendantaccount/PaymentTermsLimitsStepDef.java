package uk.gov.hmcts.opal.steps.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Checks maximum-length amendments through the payment-terms POST and latest GET.
 */
public class PaymentTermsLimitsStepDef extends BaseStepDef {
    private final DefendantAccountEnforcementsActions actions = new DefendantAccountEnforcementsActions();
    private JSONObject paymentTerms;

    @Given("a payment-term amendment fixture with a 250-character reason and the following values")
    public void prepareFixture(DataTable table) throws JSONException {
        Map<String, String> values = table.asMap(String.class, String.class);
        String sample = "Payment amendment " + values.get("payment_terms_type_code") + " "
            + values.get("instalment_period_code") + " agreed after reviewing the defendant's circumstances. ";
        String reason = sample.repeat(250 / sample.length() + 1).substring(0, 249) + "!";
        assertEquals(250, reason.length(), "Submitted reason length");
        int daysInDefault = Integer.parseInt(values.get("days_in_default"));
        assertEquals(5, String.valueOf(daysInDefault).length(), "Days in default input length");
        assertTrue(daysInDefault > 0 && daysInDefault <= 99999, "Days in default must fit five numeric characters");

        paymentTerms = new JSONObject()
            .put("days_in_default", daysInDefault)
            .put("date_days_in_default_imposed", LocalDate.now().toString())
            .put("extension", true)
            .put("reason_for_extension", reason)
            .put("payment_terms_type", new JSONObject()
                .put("payment_terms_type_code", values.get("payment_terms_type_code"))
                .put("payment_terms_type_display_name", values.get("payment_terms_type_display_name")))
            .put("effective_date", LocalDate.now().plusDays(30).toString())
            .put("instalment_period", values.get("instalment_period_code").equals("null") ? JSONObject.NULL
                : new JSONObject().put("instalment_period_code", values.get("instalment_period_code"))
                    .put("instalment_period_display_name", values.get("instalment_period_display_name")))
            .put("lump_sum_amount", amount(values.get("lump_sum_amount")))
            .put("instalment_amount", amount(values.get("instalment_amount")))
            .put("posted_details", JSONObject.NULL);
    }

    @When("I submit the maximum-length payment-term amendment for business unit {string}")
    public void submitAmendment(String businessUnitId) throws JSONException {
        Response status = actions.getCreatedDefendantAccountEnforcementStatus();
        status.then().log().ifValidationFails().statusCode(200);
        // Preserve the complete Legacy version by forwarding the account ETag unchanged.
        String etag = status.getHeader("ETag");
        assertNotNull(etag, "Enforcement status must supply the account ETag for the amendment");

        JSONObject request = new JSONObject().put("payment_terms", paymentTerms)
            .put("request_payment_card", false)
            .put("generate_payment_terms_change_letter", false);
        Response response = authorisedJsonRequest()
            .header("Business-Unit-Id", businessUnitId)
            .header("If-Match", etag)
            .body(request.toString())
            .post(accountUrl() + "/payment-terms");
        response.then().log().ifValidationFails().statusCode(200);
        assertNotNull(response.jsonPath().get("payment_terms"),
            "Successful amendment must return payment terms; HTTP 200 alone does not prove Legacy accepted it");
    }

    @Then("the latest payment terms match the submitted amendment including the reason content and length")
    public void verifyLatestTerms() throws JSONException {
        Response response = authorisedJsonRequest().get(accountUrl() + "/payment-terms/latest");
        response.then().log().ifValidationFails().statusCode(200);
        String reason = response.jsonPath().getString("payment_terms.reason_for_extension");
        assertNotNull(reason, "Latest payment terms must expose the reason for change");
        assertEquals(250, reason.length(), "Retrieved reason length");

        for (String field : List.of("reason_for_extension", "days_in_default", "date_days_in_default_imposed",
            "extension", "effective_date", "payment_terms_type.payment_terms_type_code",
            "instalment_period.instalment_period_code", "lump_sum_amount", "instalment_amount")) {
            Object expected = paymentTerms;
            for (String part : field.split("\\.")) {
                expected = expected instanceof JSONObject object ? object.opt(part) : null;
            }
            Object actual = response.jsonPath().get("payment_terms." + field);
            if (expected instanceof Number) {
                assertNotNull(actual, field);
                BigDecimal expectedNumber = new BigDecimal(expected.toString());
                assertEquals(0, expectedNumber.compareTo(new BigDecimal(actual.toString())), field);
            } else {
                assertEquals(expected == JSONObject.NULL ? null : expected, actual, field);
            }
        }
    }

    private Object amount(String value) {
        return value.equals("null") ? JSONObject.NULL : new BigDecimal(value);
    }

    private String accountUrl() {
        return getTestUrl() + "/defendant-accounts/" + scenarioContext().getCreatedDefendantAccountIdOrFail();
    }
}
