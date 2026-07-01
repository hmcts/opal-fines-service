package uk.gov.hmcts.opal.testdata;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.service.report.ReportParameterData;
import uk.gov.hmcts.opal.service.report.ReportParameterType;

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
            .supportedFileTypes(Arrays.asList(SupportedFileType.CSV, SupportedFileType.PDF, SupportedFileType.XML,
                SupportedFileType.JSON))
            .auditedReport(true)
            .reportParameters(List.of(parameter("date-param", ReportParameterType.DATE.getTypeName(), false,
                "2026-01-01", "2026-04-24", null)))
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
        return defaultReportEntityBuilder()
            .reportId("complex_params_report")
            .reportTitle("Complex Parameters Report")
            .reportGroup("Test Group")
            .reportParameters(createComplexReportParameters())
            .build();
    }

    public static List<ReportParameterData> createComplexReportParameters() {
        return List.of(
            parameter("date-param", ReportParameterType.DATE.getTypeName(), true, null, null, null),
            parameter("decimal-param", ReportParameterType.DECIMAL.getTypeName(), true, 1.0, 10.0, null),
            parameter("integer-param", ReportParameterType.INTEGER.getTypeName(), true, 1L, 10L, null),
            parameter("radio-param", ReportParameterType.MENU_RADIO.getTypeName(), true, 1, 1,
                List.of("one", "two")),
            parameter("checkbox-param", ReportParameterType.MENU_CHECKBOX.getTypeName(), true, 1, 2,
                List.of("one", "two")),
            parameter("autocomplete-param", ReportParameterType.MENU_AUTOCOMPLETE.getTypeName(), true, null, null,
                null),
            parameter("text-60-param", ReportParameterType.TEXT_MAX_60.getTypeName(), true, 1, 60, null),
            parameter("text-100-param", ReportParameterType.TEXT_MAX_100.getTypeName(), true, 1, 100, null),
            parameter("text-1000-param", ReportParameterType.TEXT_MAX_1000.getTypeName(), true, 1, 1000, null)
        );
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

    private static ReportParameterData parameter(String name, String type, boolean mandatory, Object min, Object max,
                                                 List<String> options) {
        return new ReportParameterData(name, null, type, mandatory, min, max, null, null, options, null);
    }
}


