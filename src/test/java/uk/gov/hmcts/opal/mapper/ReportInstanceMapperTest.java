package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
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
    @DisplayName("toDto()")
    class ToDto {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            ReportEntity report = buildReportEntity();
            ReportInstanceEntity instance = buildReportInstanceEntity(report);

            ReportInstanceListReportsInner result = mapper.toDto(instance, report);

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
    @DisplayName("mapReportName()")
    class MapReportName {

        @Test
        @DisplayName("Should use instance report name when set")
        void shouldUseInstanceReportNameWhenSet() {
            assertEquals("My Report", mapper.mapReportName(
                buildReportInstanceEntity(buildReportEntity()),
                buildReportEntity()
            ));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is null")
        void shouldFallBackToReportTitleWhenNameIsNull() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setReportName(null);

            assertEquals("Fallback Title", mapper.mapReportName(instance, buildReportEntity()));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is blank")
        void shouldFallBackToReportTitleWhenNameIsBlank() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setReportName("  ");

            assertEquals("Fallback Title", mapper.mapReportName(instance, buildReportEntity()));
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
            assertTrue(mapper.calculateIsDownloadable(
                buildReportInstanceEntity(buildReportEntity()),
                buildReportEntity()
            ));
        }

        @Test
        @DisplayName("Should return false when status is not READY")
        void shouldReturnFalseWhenStatusIsNotReady() {
            ReportInstanceEntity instance = buildReportInstanceEntity(buildReportEntity());
            instance.setGenerationStatus(IN_PROGRESS);

            assertFalse(mapper.calculateIsDownloadable(instance, buildReportEntity()));
        }

        @Test
        @DisplayName("Should return false when supported file types are null")
        void shouldReturnFalseWhenFileTypesAreNull() {
            ReportEntity report = ReportEntity.builder()
                .reportId("report_001")
                .reportTitle("Fallback Title")
                .supportedFileTypes(null)
                .build();

            assertFalse(mapper.calculateIsDownloadable(buildReportInstanceEntity(report), report));
        }

        @Test
        @DisplayName("Should return false when supported file types are empty")
        void shouldReturnFalseWhenFileTypesAreEmpty() {
            ReportEntity report = ReportEntity.builder()
                .reportId("report_001")
                .reportTitle("Fallback Title")
                .supportedFileTypes(List.of())
                .build();

            assertFalse(mapper.calculateIsDownloadable(buildReportInstanceEntity(report), report));
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
