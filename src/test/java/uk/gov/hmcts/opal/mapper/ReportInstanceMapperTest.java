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
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createDefaultReportEntityForInstance;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createDefaultReportInstanceEntity;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createReportEntityWithNoFileTypes;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createReportInstanceEntityWithBlankName;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createReportInstanceEntityWithNullName;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createReportInstanceEntityWithStatus;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.StatusReports;
import uk.gov.hmcts.opal.generated.model.UserByNameDetailsCommon;

class ReportInstanceMapperTest extends AbstractMapperTest {

    @Autowired
    private ReportInstanceMapper mapper;

    @Nested
    @DisplayName("toReportInstanceListReportsInner()")
    class ToReportInstanceListReportsInner {

public class ReportInstanceMapperTest {
    private final ReportInstanceMapper reportInstanceMapper = Mappers.getMapper(ReportInstanceMapper.class);
        @Test
        @DisplayName("Should map all fields correctly")
        void toReportInstanceListReportsInner() {
            ReportInstanceEntity instance = createDefaultReportInstanceEntity();
            ReportEntity report = createDefaultReportEntityForInstance();

            ReportInstanceListReportsInner result = mapper.toDto(instance, report);

            assertAll(
                () -> assertEquals(instance.getReportInstanceId(), result.getInstanceId()),
                () -> assertEquals(instance.getReportId(), result.getReportId()),
                () -> assertEquals(instance.getReportName(), result.getName()),
                () -> assertEquals(instance.getRequestedAt(), result.getRequestedAt()),
                () -> assertEquals(instance.getCreatedTimestamp(), result.getGeneratedAt()),
                () -> assertEquals(100, result.getNumberOfRecords()),
                () -> assertNotNull(result.getRequestedBy()),
                () -> assertEquals("42", result.getRequestedBy().getUserId()),
                () -> assertEquals(2, result.getBusinessUnits().size()),
                () -> assertEquals(StatusReports.CodeEnum.READY, result.getStatus().getCode()),
                () -> assertTrue(result.getIsDownloadable()),
                () -> assertEquals(2, result.getSupportedFileTypes().size())
            );
        }
    }

    @Nested
    @DisplayName("mapReportName()")
    class MapReportName {

        @Test
        @DisplayName("Should use instance report name when set")
        void mapReportName_usesInstanceName() {
            assertEquals("My Report",
                mapper.mapReportName(createDefaultReportInstanceEntity(), createDefaultReportEntityForInstance()));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is null")
        void mapReportName_fallsBackToReportTitle_whenNull() {
            assertEquals("Fallback Title",
                mapper.mapReportName(createReportInstanceEntityWithNullName(), createDefaultReportEntityForInstance()));
        }

        @Test
        @DisplayName("Should fall back to report title when instance name is blank")
        void mapReportName_fallsBackToReportTitle_whenBlank() {
            assertEquals("Fallback Title",
                mapper.mapReportName(createReportInstanceEntityWithBlankName(),
                    createDefaultReportEntityForInstance()));
        }
    }

    @Nested
    @DisplayName("mapStatus()")
    class MapStatus {

        @Test
        @DisplayName("Should map REQUESTED status with correct display name")
        void mapStatus_requested() {
            StatusReports result = mapper.mapStatus(REQUESTED);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.REQUESTED, result.getCode()),
                () -> assertEquals("Requested", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map IN_PROGRESS status with correct display name")
        void mapStatus_inProgress() {
            StatusReports result = mapper.mapStatus(IN_PROGRESS);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.IN_PROGRESS, result.getCode()),
                () -> assertEquals("In Progress", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map READY status with correct display name")
        void mapStatus_ready() {
            StatusReports result = mapper.mapStatus(READY);
            assertAll(
                () -> assertEquals(StatusReports.CodeEnum.READY, result.getCode()),
                () -> assertEquals("Ready", result.getDisplayName())
            );
        }

        @Test
        @DisplayName("Should map ERROR status with correct display name")
        void mapStatus_error() {
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
        void calculateIsDownloadable_trueWhenReadyWithFileTypes() {
            assertTrue(mapper.calculateIsDownloadable(
                createDefaultReportInstanceEntity(), createDefaultReportEntityForInstance()));
        }

        @Test
        @DisplayName("Should return false when status is not READY")
        void calculateIsDownloadable_falseWhenNotReady() {
            assertFalse(mapper.calculateIsDownloadable(
                createReportInstanceEntityWithStatus(IN_PROGRESS), createDefaultReportEntityForInstance()));
        }

        @Test
        @DisplayName("Should return false when supported file types are null")
        void calculateIsDownloadable_falseWhenFileTypesNull() {
            assertFalse(mapper.calculateIsDownloadable(
                createDefaultReportInstanceEntity(), createReportEntityWithNoFileTypes()));
        }

        @Test
        @DisplayName("Should return false when supported file types are empty")
        void calculateIsDownloadable_falseWhenFileTypesEmpty() {
            ReportEntity report = ReportEntity.builder()
                .reportId("report_001").reportTitle("Fallback Title").supportedFileTypes(List.of()).build();
            assertFalse(mapper.calculateIsDownloadable(createDefaultReportInstanceEntity(), report));
        }
    }

    @Nested
    @DisplayName("mapSupportedFileTypes()")
    class MapSupportedFileTypes {

        @Test
        @DisplayName("Should map supported file types correctly")
        void mapSupportedFileTypes_mapsCorrectly() {
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
        void mapSupportedFileTypes_emptyWhenNull() {
            assertTrue(mapper.mapSupportedFileTypes(null).isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when types is empty")
        void mapSupportedFileTypes_emptyWhenEmpty() {
            assertTrue(mapper.mapSupportedFileTypes(List.of()).isEmpty());
        }
    }

    @Nested
    @DisplayName("mapRequestedBy()")
    class MapRequestedBy {

        @Test
        @DisplayName("Should map both userId and name")
        void mapRequestedBy_withBothFields() {
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
    public void toResponseDto_shouldMapFromEntity() {
        ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
            .report(ReportEntity.builder().reportId("REPORT-ID-123").build())
            .reportInstanceId(456L)
            .build();
        CreateReportInstanceResponseReports  responseDto = reportInstanceMapper.toResponseDto(reportInstanceEntity);
        @Test
        @DisplayName("Should map name only when userId is null")
        void mapRequestedBy_withNameOnly() {
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

        assertNotNull(responseDto);
        assertEquals(456L, responseDto.getReportInstanceId());
    }
}
        @Test
        @DisplayName("Should return null when input instance is null")
        void mapRequestedBy_returnsNullWhenInputNull() {
            assertNull(mapper.mapRequestedBy(null));
        }
    }

    @Nested
    @DisplayName("mapBusinessUnits()")
    class MapBusinessUnits {

        @Test
        @DisplayName("Should map business unit IDs to BusinessUnitSummaryCommon")
        void mapBusinessUnits_mapsCorrectly() {
            var result = mapper.mapBusinessUnits(List.of(10L, 20L));
            assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals("10", result.get(0).getBusinessUnitId()),
                () -> assertEquals("20", result.get(1).getBusinessUnitId())
            );
        }

        @Test
        @DisplayName("Should return empty list when null is passed")
        void mapBusinessUnits_emptyWhenNull() {
            assertTrue(mapper.mapBusinessUnits(null).isEmpty());
        }
    }
}

