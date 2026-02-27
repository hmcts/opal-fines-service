package uk.gov.hmcts.opal.controllers.util;

public class LegacyDefendantsUtil {

    private LegacyDefendantsUtil() {
    }

    public static String getPaymentTermsRequestSampleAsJson() {
        return """
            {
              "payment_terms": {
                "days_in_default": 14,
                "date_days_in_default_imposed": "2026-02-23",
                "extension": false,
                "reason_for_extension": null,
                "payment_terms_type": {
                  "payment_terms_type_code": "I",
                  "payment_terms_type_display_name": "Instalments"
                },
                "effective_date": "2026-03-01",
                "instalment_period": {
                  "instalment_period_code": "M",
                  "instalment_period_display_name": "Monthly"
                },
                "lump_sum_amount": 100.00,
                "instalment_amount": 25.00,
                "posted_details": {
                  "posted_date": "2026-02-23T10:30:00Z",
                  "posted_by": "SYSTEM",
                  "posted_by_name": "System User"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": true
            }
            """;
    }
}
