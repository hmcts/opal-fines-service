package uk.gov.hmcts.opal.testdata;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.ReportReports;

/**
 * Test data factory for creating Report-related test objects. Provides reusable test data builders for ReportEntity and
 * ReportReports DTOs.
 */
public class ReportTestData {

    // Default test report IDs
    public static final String DEFAULT_REPORT_ID = "operational_report_enforcement";
    public static final String PAYMENT_REPORT_ID = "operational_report_payment";
    public static final String TEST_REPORT_ID = "test_report";

    /**
     * Creates a default ReportEntity for testing.
     */
    public static ReportEntity createDefaultReportEntity() {
        return defaultReportEntityBuilder().build();
    }

    /**
     * Creates a ReportEntity with the specified report ID.
     */
    public static ReportEntity createReportEntity(String reportId) {
        return defaultReportEntityBuilder()
            .reportId(reportId)
            .build();
    }

    /**
     * Creates a ReportEntity with all fields populated.
     */
    public static ReportEntity createFullReportEntity() {
        return defaultReportEntityBuilder()
            .reportId("test_report_full")
            .reportTitle("Full Test Report")
            .reportGroup("Test Reports")
            .supportedFileTypes(Arrays.asList(SupportedFileType.CSV, SupportedFileType.PDF, SupportedFileType.XML))
            .auditedReport(true)
            .reportParameters("{\"fromDate\":\"2026-01-01\",\"toDate\":\"2026-04-24\"}")
            .supportsMultiBu(true)
            .isBespokeJourney(true)
            .shownAsWorklist(true)
            .retentionPeriod(Duration.ofDays(30))
            .canManuallyCreate(false)
            .build();
    }

    /**
     * Creates a ReportEntity with minimal required fields only.
     */
    public static ReportEntity createMinimalReportEntity() {
        return defaultReportEntityBuilder()
            .reportId("minimal_report")
            .reportTitle("Minimal Report")
            .reportGroup("Test Group")
            .supportedFileTypes(null)
            .build();
    }

    /**
     * Creates a ReportEntity with null retention period.
     */
    public static ReportEntity createReportEntityWithNullRetentionPeriod() {
        return defaultReportEntityBuilder()
            .reportId("report_no_retention")
            .reportTitle("Report Without Retention")
            .reportGroup("Test Group")
            .retentionPeriod(null)
            .build();
    }

    /**
     * Creates a ReportEntity with complex report parameters.
     */
    public static ReportEntity createReportEntityWithComplexParameters() {
        String complexJson = """
            {
                "filters": {
                    "dateRange": {
                        "from": "2026-01-01",
                        "to": "2026-12-31"
                    },
                    "status": ["ACTIVE", "PENDING"]
                },
                "options": {
                    "includeArchived": false
                }
            }""";

        return defaultReportEntityBuilder()
            .reportId("complex_params_report")
            .reportTitle("Complex Parameters Report")
            .reportGroup("Test Group")
            .reportParameters(complexJson)
            .build();
    }

    /**
     * Creates a default ReportReports DTO for testing.
     */
    public static ReportReports createDefaultReportDto() {
        return createReportDto(DEFAULT_REPORT_ID);
    }

    /**
     * Creates a ReportReports DTO with the specified report ID.
     */
    public static ReportReports createReportDto(String reportId) {
        ReportReports dto = new ReportReports();
        dto.setReportId(reportId);
        dto.setReportTitle("Operational report (by enforcement)");
        dto.setReportGroup("Operational Reports");
        dto.setSupportedFileTypes(List.of(
            ReportReports.SupportedFileTypesEnum.CSV,
            ReportReports.SupportedFileTypesEnum.PDF
        ));
        dto.setAuditedReport(false);
        dto.setSupportsMultipleBusinessUnits(false);
        dto.setIsBespokeJourney(false);
        dto.setShownAsWorklist(false);
        dto.setRetentionPeriod("14");
        dto.setCanManuallyCreate(true);
        return dto;
    }

    /**
     * Creates a fully populated ReportReports DTO with all fields set.
     */
    public static ReportReports createFullReportDto() {
        ReportReports dto = createDefaultReportDto();
        dto.setReportId("test_report_full");
        dto.setReportTitle("Full Test Report");
        dto.setReportGroup("Test Reports");
        dto.setSupportedFileTypes(List.of(
            ReportReports.SupportedFileTypesEnum.CSV,
            ReportReports.SupportedFileTypesEnum.PDF,
            ReportReports.SupportedFileTypesEnum.XML
        ));
        dto.setAuditedReport(true);
        dto.setSupportsMultipleBusinessUnits(true);
        dto.setIsBespokeJourney(true);
        dto.setShownAsWorklist(true);
        dto.setRetentionPeriod("P30D");
        dto.setCanManuallyCreate(false);
        return dto;
    }

    /**
     * Creates a builder for customizing ReportEntity instances.
     */
    public static ReportEntity.ReportEntityBuilder defaultReportEntityBuilder() {
        return ReportEntity.builder()
            .reportId(DEFAULT_REPORT_ID)
            .reportTitle("Operational report (by enforcement)")
            .reportGroup("Operational Reports")
            .supportedFileTypes(Arrays.asList(SupportedFileType.CSV, SupportedFileType.PDF))
            .auditedReport(false)
            .supportsMultiBu(false)
            .isBespokeJourney(false)
            .shownAsWorklist(false)
            .retentionPeriod(Duration.ofDays(14))
            .permission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)
            .canManuallyCreate(true);
    }
}



