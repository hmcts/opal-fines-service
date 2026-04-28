package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createFullReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createMinimalReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithComplexParameters;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithNullRetentionPeriod;
import static uk.gov.hmcts.opal.testdata.ReportTestData.defaultReportEntityBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.helper.DurationMapperHelper;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@SpringJUnitConfig(classes = {ReportMapperImpl.class, ObjectMapper.class, JsonMapperHelper.class,
    DurationMapperHelper.class})
@DisplayName("ReportMapper Tests")
class ReportMapperTest {

    @Autowired
    private ReportMapper cut;

    @Nested
    @DisplayName("toDto() - Mapping All Fields")
    class MappingAllFields {

        @Test
        @DisplayName("Should map all fields when entity has complete data")
        void toDto_shouldMapAllFields() {
            ReportEntity entity = createFullReportEntity();

            ReportReports actual = cut.toDto(entity);

            assertNotNull(actual, "Result should not be null");

            assertAll("Verify basic fields",
                () -> assertEquals(entity.getReportId(), actual.getReportId(), "Report ID should match"),
                () -> assertEquals(entity.getReportTitle(), actual.getReportTitle(), "Report title should match"),
                () -> assertEquals(entity.getReportGroup(), actual.getReportGroup(), "Report group should match")
            );

            assertAll("Verify supported file types",
                () -> assertEquals(3, actual.getSupportedFileTypes().size(), "Should have 3 file types"),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.CSV)),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.PDF)),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.XML))
            );

            assertAll("Verify report parameters",
                () -> assertNotNull(actual.getReportParameters(), "Report parameters should not be null"),
                () -> {
                    Map<?, ?> params = actual.getReportParameters();
                    assertNotNull(params, "Parameters map should not be null");
                    assertEquals("2026-01-01", params.get("fromDate"));
                    assertEquals("2026-04-24", params.get("toDate"));
                }
            );

            assertAll("Verify boolean flags",
                () -> assertEquals(entity.getAuditedReport(), actual.getAuditedReport()),
                () -> assertEquals(entity.getSupportsMultiBu(), actual.getSupportsMultipleBusinessUnits()),
                () -> assertEquals(entity.getIsBespokeJourney(), actual.getIsBespokeJourney()),
                () -> assertEquals(entity.getShownAsWorklist(), actual.getShownAsWorklist()),
                () -> assertEquals(entity.getCanManuallyCreate(), actual.getCanManuallyCreate())
            );

            assertAll("Verify additional fields",
                () -> assertEquals("P30D", actual.getRetentionPeriod(),
                    "Retention period should match"),
                () -> assertEquals(entity.getPermission(), actual.getPermission(), "Permission should match")
            );
        }
    }

    @Nested
    @DisplayName("toDto() - Report Parameters Handling")
    class ReportParametersHandling {

        @Test
        @DisplayName("Should return null when report parameters is null")
        void toDto_withNullReportParameters_shouldReturnNull() {
            ReportEntity entity = createDefaultReportEntity();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify entity with null parameters",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertTrue(actual.getReportParameters() == null || actual.getReportParameters().isEmpty(),
                    "Report parameters should be null or empty")
            );

            assertAll("Verify boolean flags for default entity",
                () -> assertFalse(actual.getAuditedReport(), "Audited report should be false"),
                () -> assertFalse(actual.getSupportsMultipleBusinessUnits(), "Supports multi BU should be false"),
                () -> assertFalse(actual.getIsBespokeJourney(), "Is bespoke journey should be false"),
                () -> assertFalse(actual.getShownAsWorklist(), "Shown as worklist should be false"),
                () -> assertTrue(actual.getCanManuallyCreate(), "Can manually create should be true")
            );
        }

        @Test
        @DisplayName("Should return null when report parameters is empty JSON object")
        void toDto_withEmptyReportParameters_shouldReturnNull() {
            ReportEntity entity = defaultReportEntityBuilder()
                .reportParameters("{}")
                .build();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify empty parameters handling",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertTrue(actual.getReportParameters() == null || actual.getReportParameters().isEmpty(),
                    "Empty JSON object should return null or empty map")
            );
        }

        @Test
        @DisplayName("Should parse complex JSON parameters correctly")
        void toDto_withComplexReportParameters_shouldParseCorrectly() {
            ReportEntity entity = createReportEntityWithComplexParameters();

            ReportReports actual = cut.toDto(entity);

            assertNotNull(actual, "Result should not be null");
            assertNotNull(actual.getReportParameters(), "Report parameters should not be null");

            Map<?, ?> params = actual.getReportParameters();
            assertAll("Verify complex JSON structure",
                () -> assertTrue(params.containsKey("filters"), "Should contain filters key"),
                () -> assertTrue(params.containsKey("options"), "Should contain options key"),
                () -> assertNotNull(params.get("filters"), "Filters should not be null"),
                () -> assertNotNull(params.get("options"), "Options should not be null")
            );
        }
    }

    @Nested
    @DisplayName("toDto() - Supported File Types Handling")
    class SupportedFileTypesHandling {

        @Test
        @DisplayName("Should return empty list when supported file types is empty")
        void toDto_withEmptySupportedFileTypes_shouldReturnEmptyList() {
            ReportEntity entity = defaultReportEntityBuilder()
                .supportedFileTypes(List.of())
                .build();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify empty file types handling",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertNotNull(actual.getSupportedFileTypes(), "Supported file types should not be null"),
                () -> assertTrue(actual.getSupportedFileTypes().isEmpty(), "Supported file types should be empty")
            );
        }

        @Test
        @DisplayName("Should return empty list when supported file types is null")
        void toDto_withNullSupportedFileTypes_shouldReturnEmptyList() {
            ReportEntity entity = createMinimalReportEntity();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify null file types handling",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertNotNull(actual.getSupportedFileTypes(), "Supported file types should not be null"),
                () -> assertTrue(actual.getSupportedFileTypes().isEmpty(), "Null file types should return empty list")
            );
        }

        @Test
        @DisplayName("Should filter out invalid file type enums")
        void toDto_withInvalidFileType_shouldFilterOutInvalidTypes() {
            ReportEntity entity = defaultReportEntityBuilder()
                .supportedFileTypes(Arrays.asList("CSV", "INVALID_TYPE", "PDF"))
                .build();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify invalid file type filtering",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(2, actual.getSupportedFileTypes().size(), "Should have 2 valid file types"),
                () -> assertEquals(ReportReports.SupportedFileTypesEnum.CSV, actual.getSupportedFileTypes().getFirst(),
                    "First file type should be CSV"),
                () -> assertEquals(ReportReports.SupportedFileTypesEnum.PDF, actual.getSupportedFileTypes().get(1),
                    "Second file type should be PDF")
            );
        }
    }

    @Nested
    @DisplayName("toDto() - Retention Period Handling")
    class RetentionPeriodHandling {

        @Test
        @DisplayName("Should return null when retention period is null")
        void toDto_withNullRetentionPeriod_shouldReturnNull() {
            ReportEntity entity = createReportEntityWithNullRetentionPeriod();

            ReportReports actual = cut.toDto(entity);

            assertAll("Verify null retention period handling",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertNull(actual.getRetentionPeriod(), "Retention period should be null")
            );
        }
    }
}

