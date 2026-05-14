package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.IN_PROGRESS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.CSV;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.PDF;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.entity.report.ReportSupportedFileType;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;
import uk.gov.hmcts.opal.generated.model.ReportReferenceReports.SupportedFileTypesEnum;
import uk.gov.hmcts.opal.generated.model.StatusReports.CodeEnum;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;
import uk.gov.hmcts.opal.mapper.common.JsonMapper;
import uk.gov.hmcts.opal.service.report.ReportError;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.StatusReports;
import uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon;

class ReportInstanceMapperTest extends AbstractMapperTest {

    @Autowired
    private ReportInstanceMapper mapper;

    private ReportEntity buildReportEntity() {
        return ReportEntity.builder()
            .reportId("report_001")
            .reportTitle("Fallback Title")
            .supportedFileTypes(List.of(CSV, PDF))
            .build();
    }

    private ReportInstanceEntity buildReportInstanceEntity(ReportEntity report) {
        return ReportInstanceEntity.builder()
            .reportInstanceId(1L)
            .report(report)
            .reportName("My Report")
            .requestedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
            .createdTimestamp(LocalDateTime.of(2026, 1, 1, 11, 0))
            .requestedBy(42L)
            .requestedByName("John Doe")
            .businessUnit(List.of(10, 20))
            .generationStatus(ReportInstanceGenerationStatus.READY)
            .noOfRecords(100L)
            .build();
    }

    @Nested
    @DisplayName("toReportInstanceListReportsInnerDto()")
    class ToReportInstanceListReportsInnerDto {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            ReportEntity report = buildReportEntity();
            ReportInstanceEntity instance = buildReportInstanceEntity(report);

            ReportInstanceListReportsInner result = mapper.toReportInstanceListReportsInnerDto(instance);

            assertAll(
                () -> assertEquals(instance.getReportInstanceId(), result.getInstanceId()),
                () -> assertEquals(report.getReportId(), result.getReportId()),
                () -> assertEquals(instance.getReportName(), result.getName()),
                () -> assertEquals(instance.getRequestedAt(), result.getRequestedAt()),
                () -> assertEquals(instance.getCreatedTimestamp(), result.getGeneratedAt()),
                () -> assertEquals(100, result.getNumberOfRecords()),
                () -> assertNotNull(result.getRequestedBy()),
                () -> assertEquals("42", result.getRequestedBy().getUserId()),
                () -> assertEquals("John Doe", result.getRequestedBy().getName()),
                () -> assertEquals(2, result.getBusinessUnits().size()),
                () -> assertEquals("10", result.getBusinessUnits().get(0).getBusinessUnitId()),
                () -> assertEquals("20", result.getBusinessUnits().get(1).getBusinessUnitId()),
                () -> assertEquals(StatusReports.CodeEnum.READY, result.getStatus().getCode()),
                () -> assertEquals("Ready", result.getStatus().getDisplayName()),
                () -> assertTrue(result.getIsDownloadable()),
                () -> assertEquals(2, result.getSupportedFileTypes().size())
            );
        }
    }

    @Nested
    @DisplayName("toResponseDto()")
    class ToResponseDto {

        @Test
        @DisplayName("Should map report instance id from entity")
        void shouldMapReportInstanceIdFromEntity() {
            ReportInstanceEntity entity = ReportInstanceEntity.builder()
                .report(ReportEntity.builder().reportId("REPORT-ID-123").build())
                .reportInstanceId(456L)
                .build();

            CreateReportInstanceResponseReports responseDto = mapper.toResponseDto(entity);

            assertAll(
                () -> assertNotNull(responseDto),
                () -> assertEquals(456L, responseDto.getReportInstanceId())
            );
        }
    }

    @Nested
    @DisplayName("toReportInstanceReportsDto()")
    class ToReportInstanceReportsDto {
        private static final long REPORT_INSTANCE_ID = 123L;
        private static final String REPORT_ID = "REPORT-ID-1";
        private static final String REPORT_TITLE = "Report 1 Title";
        private static final LocalDateTime REQUESTED_AT = LocalDateTime.now().minusDays(1);
        private static final LocalDateTime GENERATED_AT = LocalDateTime.now();
        private static final LocalDateTime SCHEDULED_DELETION = LocalDateTime.now().plusDays(1);
        private static final short NO_OF_RECORDS = (short) 10;

        @Autowired
        private ReportInstanceMapper reportInstanceMapper;

        @Autowired
        private BusinessUnitSummaryMapper businessUnitSummaryMapper;

        @Autowired
        private JsonMapper jsonMapper;

        @Configuration
        @ComponentScan(basePackageClasses = ReportInstanceMapper.class)
        static class TestConfig {

        }

        @Test
        public void test_completedReportInstanceNoErrors() {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .reportInstanceId(REPORT_INSTANCE_ID)
                .requestedAt(REQUESTED_AT)
                .createdTimestamp(GENERATED_AT)
                .report(ReportEntity.builder().reportId(REPORT_ID).reportTitle(REPORT_TITLE).supportedFileTypes(List.of(
                    ReportSupportedFileType.JSON, ReportSupportedFileType.CSV)).build())
                .reportName(null) //todo additional test for this not null
                .businessUnit(List.of(1L,2L))
                .generationStatus(ReportInstanceGenerationStatus.READY)
                .noOfRecords(NO_OF_RECORDS)
                .errors(null)
                .reportParameters("{\"report_param_1\":\"param_value\"}")
                .scheduledDeletionTimestamp(SCHEDULED_DELETION)
                .build();

            List<BusinessUnitEntity> businessUnitEntityList = Lists.list(
                BusinessUnitEntity.builder().businessUnitId((short)1).businessUnitName("BU_1").welshLanguage(true).build(),
                BusinessUnitEntity.builder().businessUnitId((short)2).businessUnitName("BU_2").welshLanguage(false).build()
            );

            ReportInstanceReports response = reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntityList);

            //asserts
            assertNotNull(response);
            assertEquals(REPORT_INSTANCE_ID, response.getInstanceId().longValue());
            assertEquals(REQUESTED_AT, response.getRequestedAt());
            assertEquals(GENERATED_AT, response.getGeneratedAt());
            assertEquals(REPORT_TITLE, response.getName());

            //business units
            MatcherAssert.assertThat(response.getBusinessUnits(), Matchers.containsInAnyOrder(
                    Matchers.allOf(
                        Matchers.hasProperty("businessUnitName", Matchers.is("BU_1")),
                        Matchers.hasProperty("businessUnitId", Matchers.is("1")),
                        Matchers.hasProperty("welshSpeaking", Matchers.is("Y"))),
                    Matchers.allOf(
                        Matchers.hasProperty("businessUnitName", Matchers.is("BU_2")),
                        Matchers.hasProperty("businessUnitId", Matchers.is("2")),
                        Matchers.hasProperty("welshSpeaking", Matchers.is("N")))
                )
            );

            assertEquals(CodeEnum.READY, response.getStatus().getCode());
            assertEquals(ReportInstanceGenerationStatus.READY.toString(), response.getStatus().getDisplayName());

            assertEquals(NO_OF_RECORDS, response.getNumberOfRecords().shortValue());
            assertEquals(true, response.getIsDownloadable());

            assertNull(response.getErrors()); //no errors

            MatcherAssert.assertThat(response.getReportParameters(), Matchers.allOf(
                Matchers.hasEntry("report_param_1", "param_value")
            ));

            assertEquals(SCHEDULED_DELETION, response.getRetainUntil());

            assertEquals(REPORT_ID, response.getReport().getId());
            MatcherAssert.assertThat(response.getReport().getSupportedFileTypes(), Matchers.containsInAnyOrder(
                SupportedFileTypesEnum.JSON, SupportedFileTypesEnum.CSV));
        }

        @Test
        public void test_customReportInstanceNameOverridesReportTitle() {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .reportInstanceId(REPORT_INSTANCE_ID)
                .requestedAt(REQUESTED_AT)
                .createdTimestamp(GENERATED_AT)
                .report(ReportEntity.builder().reportId(REPORT_ID).reportTitle(REPORT_TITLE).supportedFileTypes(List.of(
                    ReportSupportedFileType.JSON, ReportSupportedFileType.CSV)).build())
                .reportName("Custom Name")
                .businessUnit(List.of(1L,2L))
                .generationStatus(ReportInstanceGenerationStatus.READY)
                .noOfRecords(NO_OF_RECORDS)
                .errors(null)
                .reportParameters("{\"report_param_1\":\"param_value\"}")
                .scheduledDeletionTimestamp(SCHEDULED_DELETION)
                .build();

            List<BusinessUnitEntity> businessUnitEntityList = Lists.list(
                BusinessUnitEntity.builder().businessUnitId((short)1).businessUnitName("BU_1").welshLanguage(true).build(),
                BusinessUnitEntity.builder().businessUnitId((short)2).businessUnitName("BU_2").welshLanguage(false).build()
            );

            ReportInstanceReports response = reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntityList);

            assertNotNull(response);
            assertEquals("Custom Name", response.getName());
        }

        @Test
        public void test_errors() {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .reportInstanceId(REPORT_INSTANCE_ID)
                .requestedAt(REQUESTED_AT)
                .createdTimestamp(GENERATED_AT)
                .report(ReportEntity.builder().reportId(REPORT_ID).reportTitle(REPORT_TITLE).supportedFileTypes(List.of(
                    ReportSupportedFileType.JSON, ReportSupportedFileType.CSV)).build())
                .reportName(null)
                .businessUnit(List.of(1L,2L))
                .generationStatus(ReportInstanceGenerationStatus.ERROR)
                .noOfRecords(NO_OF_RECORDS)
                .errors(ReportError.builder().operationId("ERROR-ID").error("Unit test error").build())
                .reportParameters(null)
                .scheduledDeletionTimestamp(SCHEDULED_DELETION)
                .build();
            List<BusinessUnitEntity> businessUnitEntityList = Lists.list(
                BusinessUnitEntity.builder().businessUnitId((short)1).businessUnitName("BU_1").welshLanguage(true).build(),
                BusinessUnitEntity.builder().businessUnitId((short)2).businessUnitName("BU_2").welshLanguage(false).build()
            );

            ReportInstanceReports response = reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntityList);

            assertNotNull(response);
            assertEquals(CodeEnum.ERROR, response.getStatus().getCode());
            assertEquals(CodeEnum.ERROR.getValue(), response.getStatus().getDisplayName());
            assertNull(response.getReportParameters());

            assertNotNull(response.getErrors());
            assertEquals(1, response.getErrors().size());
            MatcherAssert.assertThat(response.getErrors().getFirst(), Matchers.allOf(
                Matchers.aMapWithSize(2),
                Matchers.hasEntry("operationId", "ERROR-ID"),
                Matchers.hasEntry("error", "Unit test error")
            ));
        }

        @Test
        public void test_reportInstanceReadyButSupportedFiletypesIsNull() {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .reportInstanceId(REPORT_INSTANCE_ID)
                .requestedAt(REQUESTED_AT)
                .createdTimestamp(GENERATED_AT)
                .report(ReportEntity.builder().reportId(REPORT_ID).reportTitle(REPORT_TITLE).supportedFileTypes(null).build())
                .reportName(null)
                .businessUnit(List.of(1L,2L))
                .generationStatus(ReportInstanceGenerationStatus.READY)
                .noOfRecords(NO_OF_RECORDS)
                .errors(ReportError.builder().operationId("ERROR-ID").error("Unit test error").build())
                .reportParameters(null)
                .scheduledDeletionTimestamp(SCHEDULED_DELETION)
                .build();
            List<BusinessUnitEntity> businessUnitEntityList = Lists.list(
                BusinessUnitEntity.builder().businessUnitId((short)1).businessUnitName("BU_1").welshLanguage(true).build(),
                BusinessUnitEntity.builder().businessUnitId((short)2).businessUnitName("BU_2").welshLanguage(false).build()
            );

            ReportInstanceReports response = reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntityList);
            assertNotNull(response);
            assertEquals(CodeEnum.READY, response.getStatus().getCode());
            assertEquals(CodeEnum.READY.getValue(), response.getStatus().getDisplayName());
            assertEquals(false, response.getIsDownloadable());
        }

        @Test
        public void test_reportInstanceReadyButSupportedFiletypesIsEmpty() {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .reportInstanceId(REPORT_INSTANCE_ID)
                .requestedAt(REQUESTED_AT)
                .createdTimestamp(GENERATED_AT)
                .report(ReportEntity.builder().reportId(REPORT_ID).reportTitle(REPORT_TITLE).supportedFileTypes(Collections.emptyList()).build())
                .reportName(null)
                .businessUnit(List.of(1L,2L))
                .generationStatus(ReportInstanceGenerationStatus.READY)
                .noOfRecords(NO_OF_RECORDS)
                .errors(ReportError.builder().operationId("ERROR-ID").error("Unit test error").build())
                .reportParameters(null)
                .scheduledDeletionTimestamp(SCHEDULED_DELETION)
                .build();
            List<BusinessUnitEntity> businessUnitEntityList = Lists.list(
                BusinessUnitEntity.builder().businessUnitId((short)1).businessUnitName("BU_1").welshLanguage(true).build(),
                BusinessUnitEntity.builder().businessUnitId((short)2).businessUnitName("BU_2").welshLanguage(false).build()
            );

            ReportInstanceReports response = reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntityList);
            assertNotNull(response);
            assertEquals(CodeEnum.READY, response.getStatus().getCode());
            assertEquals(CodeEnum.READY.getValue(), response.getStatus().getDisplayName());
            assertEquals(false, response.getIsDownloadable());
        }
    }


    @Nested
    @DisplayName("mapReportName()")
    class MapReportName {

        @Test
        @DisplayName("Should use instance report name when set")
        void shouldUseInstanceReportNameWhenSet() {
            assertEquals("My Report", mapper.mapReportName(buildReportInstanceEntity(buildReportEntity())));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is null")
        void shouldFallBackToReportTitleWhenNameIsNull() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setReportName(null);

            assertEquals("Fallback Title", mapper.mapReportName(instance));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is blank")
        void shouldFallBackToReportTitleWhenNameIsBlank() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setReportName("  ");

            assertEquals("Fallback Title", mapper.mapReportName(instance));
        }
    }

    @Nested
    @DisplayName("mapStatus()")
    class MapStatus {

        @Test
        @DisplayName("Should map REQUESTED status with correct display name")
        void shouldMapRequestedStatus() {
            StatusReports result = mapper.mapStatus(REQUESTED);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.REQUESTED, result.getCode()),
                () -> assertEquals("Requested", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map IN_PROGRESS status with correct display name")
        void shouldMapInProgressStatus() {
            StatusReports result = mapper.mapStatus(IN_PROGRESS);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.IN_PROGRESS, result.getCode()),
                () -> assertEquals("In Progress", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map READY status with correct display name")
        void shouldMapReadyStatus() {
            StatusReports result = mapper.mapStatus(READY);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.READY, result.getCode()),
                () -> assertEquals("Ready", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map ERROR status with correct display name")
        void shouldMapErrorStatus() {
            StatusReports result = mapper.mapStatus(ERROR);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.ERROR, result.getCode()),
                () -> assertEquals("Error", result.getDisplayName())
            );
        }
    }

    @Nested
    @DisplayName("calculateIsDownloadable()")
    class CalculateIsDownloadable {

        @Test
        @DisplayName("Should return true when READY and has supported file types")
        void shouldReturnTrueWhenReadyWithFileTypes() {
            assertTrue(mapper.calculateIsDownloadable(buildReportInstanceEntity(buildReportEntity())));
        }

        @Test
        @DisplayName("Should return false when status is not READY")
        void shouldReturnFalseWhenStatusIsNotReady() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setGenerationStatus(IN_PROGRESS);

            assertFalse(mapper.calculateIsDownloadable(instance));
        }

        @Test
        @DisplayName("Should return false when supported file types are null")
        void shouldReturnFalseWhenFileTypesAreNull() {
            ReportEntity report = ReportEntity.builder()
                .reportId("report_001")
                .reportTitle("Fallback Title")
                .supportedFileTypes(null)
                .build();

            assertFalse(mapper.calculateIsDownloadable(buildReportInstanceEntity(report)));
        }

        @Test
        @DisplayName("Should return false when supported file types are empty")
        void shouldReturnFalseWhenFileTypesAreEmpty() {
            ReportEntity report = ReportEntity.builder()
                .reportId("report_001")
                .reportTitle("Fallback Title")
                .supportedFileTypes(List.of())
                .build();

            assertFalse(mapper.calculateIsDownloadable(buildReportInstanceEntity(report)));
        }
    }

    @Nested
    @DisplayName("mapSupportedFileTypes()")
    class MapSupportedFileTypes {

        @Test
        @DisplayName("Should map supported file types correctly")
        void shouldMapSupportedFileTypesCorrectly() {
            List<ReportInstanceListReportsInner.SupportedFileTypesEnum> result =
                mapper.mapSupportedFileTypes(List.of(CSV, PDF));

            assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.contains(ReportInstanceListReportsInner.SupportedFileTypesEnum.CSV)),
                () -> assertTrue(result.contains(ReportInstanceListReportsInner.SupportedFileTypesEnum.PDF))
            );
        }

        @Test
        @DisplayName("Should return empty list when types is null")
        void shouldReturnEmptyListWhenTypesAreNull() {
            assertTrue(mapper.mapSupportedFileTypes(null).isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when types is empty")
        void shouldReturnEmptyListWhenTypesAreEmpty() {
            assertTrue(mapper.mapSupportedFileTypes(List.of()).isEmpty());
        }
    }

    @Nested
    @DisplayName("mapRequestedBy()")
    class MapRequestedBy {

        @Test
        @DisplayName("Should map both userId and name")
        void shouldMapBothUserIdAndName() {
            ReportInstanceEntity instance = ReportInstanceEntity.builder()
                .requestedBy(42L)
                .requestedByName("Jane Doe")
                .build();

            UserByNameDetailsCommon result = mapper.mapRequestedBy(instance);

            assertAll(
                () -> assertEquals("42", result.getUserId()),
                () -> assertEquals("Jane Doe", result.getName())
            );
        }

        @Test
        @DisplayName("Should map name only when userId is null")
        void shouldMapNameOnlyWhenUserIdIsNull() {
            ReportInstanceEntity instance = ReportInstanceEntity.builder()
                .requestedBy(null)
                .requestedByName("Jane Doe")
                .build();

            UserByNameDetailsCommon result = mapper.mapRequestedBy(instance);

            assertAll(
                () -> assertNull(result.getUserId()),
                () -> assertEquals("Jane Doe", result.getName())
            );
        }

        @Test
        @DisplayName("Should return null when input instance is null")
        void shouldReturnNullWhenInputIsNull() {
            assertNull(mapper.mapRequestedBy(null));
        }
    }

    @Nested
    @DisplayName("mapBusinessUnits()")
    class MapBusinessUnits {

        @Test
        @DisplayName("Should map business unit IDs to BusinessUnitSummaryCommon")
        void shouldMapBusinessUnitIdsCorrectly() {
            var result = mapper.mapBusinessUnits(List.of(10, 20));

            assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("10", result.get(0).getBusinessUnitId()),
                () -> assertEquals("20", result.get(1).getBusinessUnitId())
            );
        }

        @Test
        @DisplayName("Should return empty list when null is passed")
        void shouldReturnEmptyListWhenNullIsPassed() {
            assertTrue(mapper.mapBusinessUnits(null).isEmpty());
        }
    }
}
