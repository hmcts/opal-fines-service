package uk.gov.hmcts.opal.assertions.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;

/** Verifies payment-term fixture boundaries, amendment success and persisted values. */
public class PaymentTermsAssertions {
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    public void assertMaximumLengthFixture(JSONObject paymentTerms) throws JSONException {
        assertEquals(250, paymentTerms.getString("reason_for_extension").length(), "Submitted reason length");
        int daysInDefault = paymentTerms.getInt("days_in_default");
        assertEquals(5, String.valueOf(daysInDefault).length(), "Days in default input length");
        assertTrue(daysInDefault > 0 && daysInDefault <= 99999, "Days in default must fit five numeric characters");
    }

    public void assertAccountVersion(Response response) {
        responseAssertions.assertStatus(response, 200);
        assertNotNull(response.getHeader("ETag"), "Enforcement status must supply the account ETag for the amendment");
    }

    public void assertAmendmentSucceeded(Response response) {
        responseAssertions.assertStatus(response, 200);
        assertNotNull(response.jsonPath().get("payment_terms"),
            "Successful amendment must return payment terms; HTTP 200 alone does not prove Legacy accepted it");
    }

    public void assertLatestTermsMatch(Response response, JSONObject submittedTerms) throws JSONException {
        responseAssertions.assertStatus(response, 200);
        String reason = response.jsonPath().getString("payment_terms.reason_for_extension");
        assertNotNull(reason, "Latest payment terms must expose the reason for change");
        assertEquals(250, reason.length(), "Retrieved reason length");

        for (String field : List.of("reason_for_extension", "days_in_default", "date_days_in_default_imposed",
            "extension", "effective_date", "payment_terms_type.payment_terms_type_code",
            "instalment_period.instalment_period_code", "lump_sum_amount", "instalment_amount")) {
            Object expected = submittedTerms;
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
}
