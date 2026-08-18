package uk.gov.hmcts.opal.testdata;

import static uk.gov.hmcts.opal.dto.ResultId.ABDC;
import static uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode.SINCE_DATE;
import static uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode.SINCE_LAST_ENFORCEMENT;
import static uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode.WITH_REGF;
import static uk.gov.hmcts.opal.service.report.ReportType.SUMMARY;

import java.util.List;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;

public final class OperationReportByPaymentFiltersTestData {

    private OperationReportByPaymentFiltersTestData() {
    }

    public static OperationReportByPaymentFiltersDto summarySinceDate() {
        return OperationReportByPaymentFiltersDto.builder()
            .reportType(SUMMARY)
            .reportMode(SINCE_DATE)
            .build();
    }

    public static OperationReportByPaymentFiltersDto summaryWithRegfPaymentMade() {
        return OperationReportByPaymentFiltersDto.builder()
            .reportType(SUMMARY)
            .reportMode(WITH_REGF)
            .isPaymentMade(true)
            .build();
    }

    public static OperationReportByPaymentFiltersDto summarySinceLastEnforcementPaymentMade() {
        return OperationReportByPaymentFiltersDto.builder()
            .reportType(SUMMARY)
            .sinceLastEnforcementAction(ABDC)
            .reportMode(SINCE_LAST_ENFORCEMENT)
            .isPaymentMade(true)
            .build();
    }

    public static String summarySinceDateJson() {
        return """
            {
              "reportType": "SUMMARY",
              "isPaymentMade": true,
              "reportMode": "SINCE_DATE",
              "sinceDate": "2026-05-14",
              "businessUnitIds": [77, 78]
            }
            """;
    }

    public static String summaryWithRegfPaymentMadeJson() {
        return """
            {
              "reportType": "SUMMARY",
              "isPaymentMade": true,
              "reportMode": "WITH_REGF",
              "businessUnitIds": [77, 78]
            }
            """;
    }

    public static String summarySinceLastEnforcementPaymentNotMadeJson() {
        return """
            {
              "reportType": "SUMMARY",
              "isPaymentMade": false,
              "reportMode": "SINCE_LAST_ENFORCEMENT",
              "sinceLastEnforcementAction": "ABDC",
              "businessUnitIds": [77, 78]
            }
            """;
    }

    public static ReportInstanceEntity reportInstance(String reportParameters) {
        ReportInstanceEntity reportInstance = new ReportInstanceEntity();
        reportInstance.setReportParameters(reportParameters);
        return reportInstance;
    }

    public static List<DefendantAccountEntity> singleDefendantAccountList() {
        return List.of(new DefendantAccountEntity());
    }
}
