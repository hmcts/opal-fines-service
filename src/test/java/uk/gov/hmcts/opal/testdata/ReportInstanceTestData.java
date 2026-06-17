package uk.gov.hmcts.opal.testdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.StatusReports;
import uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon;

/**
 * Test data factory for creating ReportInstance-related test objects. Provides reusable builders and factory methods
 * for ReportInstanceEntity and ReportInstanceListReportsInner DTOs.
 */
public class ReportInstanceTestData {

    public static final Long DEFAULT_INSTANCE_ID = 1L;

    // -------------------------------------------------------------------------
    // Shared constants
    // -------------------------------------------------------------------------
    public static final String DEFAULT_REPORT_ID = "report_001";
    public static final String DEFAULT_REPORT_NAME = "My Report";
    public static final Long DEFAULT_REQUESTED_BY = 42L;
    public static final String DEFAULT_REQUESTED_BY_NAME = "John Doe";
    public static final LocalDateTime DEFAULT_REQUESTED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    public static final LocalDateTime DEFAULT_CREATED_TIMESTAMP = LocalDateTime.of(2026, 1, 1, 11, 0);
    public static final List<Integer> DEFAULT_BUSINESS_UNITS = List.of(10, 20);
    public static final long DEFAULT_NO_OF_RECORDS = 100;
    public static final LocalDate FROM_DATE = LocalDate.of(2026, 1, 1);
    public static final LocalDate TO_DATE = LocalDate.of(2026, 12, 31);
    public static final List<Integer> BUSINESS_UNITS = List.of(10, 20);
    public static final Integer USER_ID = 42;

    private ReportInstanceTestData() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // ReportInstanceEntity builders and factories
    // -------------------------------------------------------------------------

    /**
     * Returns a builder pre-populated with default values for customisation.
     */
    public static ReportInstanceEntity.ReportInstanceEntityBuilder defaultReportInstanceEntityBuilder() {
        return ReportInstanceEntity.builder()
            .reportInstanceId(DEFAULT_INSTANCE_ID)
            .report(createDefaultReportEntityForInstance())
            .reportName(DEFAULT_REPORT_NAME)
            .requestedAt(DEFAULT_REQUESTED_AT)
            .createdTimestamp(DEFAULT_CREATED_TIMESTAMP)
            .requestedBy(DEFAULT_REQUESTED_BY)
            .requestedByName(DEFAULT_REQUESTED_BY_NAME)
            .businessUnit(DEFAULT_BUSINESS_UNITS)
            .generationStatus(ReportInstanceGenerationStatus.READY)
            .noOfRecords(DEFAULT_NO_OF_RECORDS);
    }

    /**
     * Creates a default fully-populated ReportInstanceEntity with READY status.
     */
    public static ReportInstanceEntity createDefaultReportInstanceEntity() {
        return defaultReportInstanceEntityBuilder().build();
    }

    /**
     * Creates a ReportInstanceEntity with the given generation status.
     */
    public static ReportInstanceEntity createReportInstanceEntityWithStatus(
        ReportInstanceGenerationStatus status) {
        return defaultReportInstanceEntityBuilder()
            .generationStatus(status)
            .build();
    }

    /**
     * Creates a ReportInstanceEntity with a null report name (forces fallback to report title).
     */
    public static ReportInstanceEntity createReportInstanceEntityWithNullName() {
        return defaultReportInstanceEntityBuilder()
            .reportName(null)
            .build();
    }

    /**
     * Creates a ReportInstanceEntity with a blank report name (forces fallback to report title).
     */
    public static ReportInstanceEntity createReportInstanceEntityWithBlankName() {
        return defaultReportInstanceEntityBuilder()
            .reportName("  ")
            .build();
    }

    /**
     * Creates a ReportInstanceEntity with no requestedBy user ID or name.
     */
    public static ReportInstanceEntity createReportInstanceEntityWithNoRequestedBy() {
        return defaultReportInstanceEntityBuilder()
            .requestedBy(null)
            .requestedByName(null)
            .build();
    }

    /**
     * Creates a ReportInstanceEntity with no business units.
     */
    public static ReportInstanceEntity createReportInstanceEntityWithNoBusinessUnits() {
        return defaultReportInstanceEntityBuilder()
            .businessUnit(null)
            .build();
    }

    public static ReportInstanceEntity reportInstance(String reportId) {
        ReportInstanceEntity instance = new ReportInstanceEntity();
        instance.setReport(ReportEntity.builder().reportId(reportId).build());
        return instance;
    }

    public static List<ReportInstanceEntity> reportInstances(String... reportIds) {
        return Arrays.stream(reportIds)
            .map(ReportInstanceTestData::reportInstance)
            .toList();
    }

    // -------------------------------------------------------------------------
    // ReportEntity factories
    // -------------------------------------------------------------------------

    /**
     * Creates a ReportEntity suitable for use alongside a ReportInstanceEntity in tests.
     */
    public static ReportEntity createDefaultReportEntityForInstance() {
        return ReportEntity.builder()
            .reportId(DEFAULT_REPORT_ID)
            .reportTitle("Fallback Title")
            .supportedFileTypes(List.of(SupportedFileType.CSV, SupportedFileType.PDF))
            .build();
    }

    /**
     * Creates a ReportEntity with no supported file types.
     */
    public static ReportEntity createReportEntityWithNoFileTypes() {
        return ReportEntity.builder()
            .reportId(DEFAULT_REPORT_ID)
            .reportTitle("Fallback Title")
            .supportedFileTypes(null)
            .build();
    }

    public static ReportEntity report(String reportId, FinesPermission permission) {
        ReportEntity report = new ReportEntity();
        report.setReportId(reportId);
        report.setPermission(permission);
        return report;
    }

    public static List<ReportEntity> reports(FinesPermission permission, String... reportIds) {
        return Arrays.stream(reportIds)
            .map(reportId -> report(reportId, permission))
            .toList();
    }

    public static BusinessUnitUser businessUnitUser(
        String businessUnitUserId,
        short businessUnitId,
        FinesPermission... permissions
    ) {
        return BusinessUnitUser.builder()
            .businessUnitUserId(businessUnitUserId)
            .businessUnitId(businessUnitId)
            .permissions(Arrays.stream(permissions).map(FinesPermission::toUserPermission).collect(
                java.util.stream.Collectors.toSet()))
            .build();
    }

    // -------------------------------------------------------------------------
    // DTO factories
    // -------------------------------------------------------------------------

    /**
     * Creates a default ReportInstanceListReportsInner DTO matching the default entity.
     */
    public static ReportInstanceListReportsInner createDefaultReportInstanceDto() {
        ReportInstanceListReportsInner dto = new ReportInstanceListReportsInner();
        dto.setInstanceId(DEFAULT_INSTANCE_ID);
        dto.setReportId(DEFAULT_REPORT_ID);
        dto.setName(DEFAULT_REPORT_NAME);
        dto.setRequestedAt(DEFAULT_REQUESTED_AT);
        dto.setGeneratedAt(DEFAULT_CREATED_TIMESTAMP);
        dto.setNumberOfRecords((int) DEFAULT_NO_OF_RECORDS);
        dto.setRequestedBy(createDefaultRequestedByDto());
        dto.setBusinessUnits(createDefaultBusinessUnitDtos());
        dto.setStatus(createStatusDto(ReportInstanceGenerationStatus.READY));
        dto.setIsDownloadable(true);
        dto.setSupportedFileTypes(List.of(
            ReportInstanceListReportsInner.SupportedFileTypesEnum.CSV,
            ReportInstanceListReportsInner.SupportedFileTypesEnum.PDF
        ));
        return dto;
    }

    /**
     * Creates a StatusReports DTO for the given generation status.
     */
    public static StatusReports createStatusDto(ReportInstanceGenerationStatus status) {
        StatusReports statusReports = new StatusReports();
        statusReports.setCode(StatusReports.CodeEnum.fromValue(status.name()));
        statusReports.setDisplayName(status.getDisplayName());
        return statusReports;
    }

    /**
     * Creates a default UserByNameDetailsCommon DTO.
     */
    public static UserByNameDetailsCommon createDefaultRequestedByDto() {
        UserByNameDetailsCommon user = new UserByNameDetailsCommon();
        user.setUserId(String.valueOf(DEFAULT_REQUESTED_BY));
        user.setName(DEFAULT_REQUESTED_BY_NAME);
        return user;
    }

    /**
     * Creates a list of BusinessUnitSummaryCommon DTOs matching the default business unit IDs.
     */
    public static List<BusinessUnitSummaryCommon> createDefaultBusinessUnitDtos() {
        return DEFAULT_BUSINESS_UNITS.stream().map(id -> {
            BusinessUnitSummaryCommon bu = new BusinessUnitSummaryCommon();
            bu.setBusinessUnitId(String.valueOf(id));
            return bu;
        }).toList();
    }
}
