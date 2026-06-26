package uk.gov.hmcts.opal.controllers.util;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ReportInstanceContentTestData {


    public static final String CASH_TILL_STORED_REPORT_JSON = """
        {
          "reportData": {
            "rows": [
              {
                "business_unit": "Cash Till Business Unit",
                "cash_till_number": "9011",
                "cashier": "opal-test",
                "payment_date_time": "2026-05-26T14:30:00",
                "destination_type": "FA",
                "details": "ACC456",
                "auto_payment": false,
                "payment_method": "NC",
                "amount": 125.50,
                "receipt": true,
                "balance": 124.50,
                "allocated": false
              }
            ],
            "allocated_report": false,
            "report_meta_data": {
              "pdpo_party_ids": []
            }
          }
        }
        """;

    public static byte[] storedReportBytes = CASH_TILL_STORED_REPORT_JSON.getBytes(UTF_8);

    private ReportInstanceContentTestData() {
    }
}
