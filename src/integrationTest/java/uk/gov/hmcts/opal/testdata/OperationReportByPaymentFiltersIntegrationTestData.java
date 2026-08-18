package uk.gov.hmcts.opal.testdata;

import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

public final class OperationReportByPaymentFiltersIntegrationTestData {

    public static final String SUMMARY_SINCE_DATE_JSON = """
        {
          "reportType": "SUMMARY",
          "isPaymentMade": true,
          "reportMode": "SINCE_DATE",
          "sinceDate": "2026-05-14",
          "businessUnitIds": [77, 78]
        }
        """;

    public static final String SUMMARY_WITH_REGF_PAYMENT_MADE_JSON = """
        {
          "reportType": "SUMMARY",
          "isPaymentMade": true,
          "reportMode": "WITH_REGF",
          "businessUnitIds": [77, 78]
        }
        """;

    public static final String SUMMARY_SINCE_LAST_ENFORCEMENT_PAYMENT_NOT_MADE_JSON = """
        {
          "reportType": "SUMMARY",
          "isPaymentMade": false,
          "reportMode": "SINCE_LAST_ENFORCEMENT",
          "sinceLastEnforcementAction": "ABDC",
          "businessUnitIds": [77, 78]
        }
        """;

    private OperationReportByPaymentFiltersIntegrationTestData() {
    }

    public static ReportInstanceEntity reportWithFilters(String json) {
        ReportInstanceEntity instance = new ReportInstanceEntity();
        instance.setReportParameters(json);
        return instance;
    }
}
