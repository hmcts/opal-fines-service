package uk.gov.hmcts.opal.actions.defendantaccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** Builds valid payment-term amendment fixtures from scenario values. */
public class PaymentTermsRequestFactory {

    public JSONObject buildMaximumLengthTerms(Map<String, String> values) throws JSONException {
        String sample = "Payment amendment " + values.get("payment_terms_type_code") + " "
            + values.get("instalment_period_code") + " agreed after reviewing the defendant's circumstances. ";
        String reason = sample.repeat(250 / sample.length() + 1).substring(0, 249) + "!";
        int daysInDefault = Integer.parseInt(values.get("days_in_default"));
        LocalDate today = LocalDate.now();
        return new JSONObject()
            .put("days_in_default", daysInDefault)
            .put("date_days_in_default_imposed", today.toString())
            .put("extension", true)
            .put("reason_for_extension", reason)
            .put("payment_terms_type", new JSONObject()
                .put("payment_terms_type_code", values.get("payment_terms_type_code"))
                .put("payment_terms_type_display_name", values.get("payment_terms_type_display_name")))
            .put("effective_date", today.plusDays(30).toString())
            .put("instalment_period", values.get("instalment_period_code").equals("null") ? JSONObject.NULL
                : new JSONObject().put("instalment_period_code", values.get("instalment_period_code"))
                    .put("instalment_period_display_name", values.get("instalment_period_display_name")))
            .put("lump_sum_amount", amount(values.get("lump_sum_amount")))
            .put("instalment_amount", amount(values.get("instalment_amount")))
            .put("posted_details", JSONObject.NULL);
    }

    public JSONObject buildAmendment(JSONObject paymentTerms) throws JSONException {
        return new JSONObject().put("payment_terms", paymentTerms)
            .put("request_payment_card", false)
            .put("generate_payment_terms_change_letter", false);
    }

    private Object amount(String value) {
        return value.equals("null") ? JSONObject.NULL : new BigDecimal(value);
    }
}
