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

    public static final String CASH_LIST_STORED_REPORT_JSON = """
        {
          "reportData": {
            "tillDetails": {
              "tillId": 99000000343100,
              "tillNumber": 9010,
              "ownedBy": "L080JG",
              "businessUnitId": 1777,
              "businessUnitName": "Cash List Business Unit",
              "businessUnitCode": "CLST"
            },
            "entries": [
              {
                "entry": 1,
                "type": "FA",
                "suspense": null,
                "accountNumber": "ACC123",
                "name": "DOE Jane",
                "nameAdditionalInformation": null,
                "paymentMethod": "NC",
                "amount": 125.50
              },
              {
                "entry": 2,
                "type": "SA",
                "suspense": "UN",
                "accountNumber": "Suspense Ref",
                "name": "1",
                "nameAdditionalInformation": "Auto - Suspense payment",
                "paymentMethod": "CT",
                "amount": 40.00
              }
            ],
            "total": 165.50,
            "reportMetaData": {
              "pdpoPartyIds": []
            }
          }
        }
        """;

    public static final byte[] cashTillStoredReportBytes = CASH_TILL_STORED_REPORT_JSON.getBytes(UTF_8);
    public static final byte[] cashListStoredReportBytes = CASH_LIST_STORED_REPORT_JSON.getBytes(UTF_8);

    private ReportInstanceContentTestData() {
    }
}
