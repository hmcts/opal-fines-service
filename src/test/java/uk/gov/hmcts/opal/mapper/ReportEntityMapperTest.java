package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.CSV;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.PDF;
import static uk.gov.hmcts.opal.entity.report.SupportedFileType.XML;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createFullReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createMinimalReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithComplexParameters;
import static uk.gov.hmcts.opal.testdata.ReportTestData.defaultReportEntityBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.config.JacksonCompatibilityConfiguration;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.helper.JsonMapperHelper;

@SpringJUnitConfig(classes = {JacksonCompatibilityConfiguration.class, ReportEntityMapperImpl.class,
    JsonMapperHelper.class})
@DisplayName("ReportEntityMapper Tests")
class ReportEntityMapperTest {

    @Autowired
    private ReportEntityMapper reportEntityMapper;

    @Nested
    @DisplayName("toDto() - Mapping All Fields")
    class MappingAllFields {

        @Test
        @DisplayName("Should map all fields when entity has complete data")
        void toDto_shouldMapAllFields() {
            ReportEntity entity = createFullReportEntity();

            ReportReports actual = reportEntityMapper.toDto(entity);

            Map<?, ?> params = actual.getReportParameters();
            assertAll(
                () -> assertEquals(entity.getReportId(), actual.getReportId()),
                () -> assertEquals(entity.getReportTitle(), actual.getReportTitle()),
                () -> assertEquals(entity.getReportGroup(), actual.getReportGroup()),
                () -> assertEquals(3, actual.getSupportedFileTypes().size()),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.CSV)),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.PDF)),
                () -> assertTrue(actual.getSupportedFileTypes().contains(ReportReports.SupportedFileTypesEnum.XML)),
                () -> assertEquals("2026-01-01", ((Map<?, ?>)params.get("date-param")).get("min")),
                () -> assertEquals("2026-04-24", ((Map<?, ?>)params.get("date-param")).get("max")),
                () -> assertEquals(entity.isAuditedReport(), actual.getAuditedReport()),
                () -> assertEquals(entity.isSupportsMultiBu(), actual.getSupportsMultipleBusinessUnits()),
                () -> assertEquals(entity.isBespokeJourney(), actual.getIsBespokeJourney()),
                () -> assertEquals(entity.isShownAsWorklist(), actual.getShownAsWorklist()),
                () -> assertEquals(entity.isCanManuallyCreate(), actual.getCanManuallyCreate()),
                () -> assertEquals("PT720H", actual.getRetentionPeriod()),
                () -> assertEquals(entity.getPermission() != null ? entity.getPermission().name() : null,
                    actual.getPermission())
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

            ReportReports actual = reportEntityMapper.toDto(entity);

            assertAll(
                () -> assertTrue(actual.getReportParameters() == null || actual.getReportParameters().isEmpty()),
                () -> assertFalse(actual.getAuditedReport()),
                () -> assertFalse(actual.getSupportsMultipleBusinessUnits()),
                () -> assertFalse(actual.getIsBespokeJourney()),
                () -> assertFalse(actual.getShownAsWorklist()),
                () -> assertTrue(actual.getCanManuallyCreate())
            );
        }

        @Test
        @DisplayName("Should return null when report parameters is empty JSON object")
        void toDto_withEmptyReportParameters_shouldReturnNull() {
            ReportEntity entity = defaultReportEntityBuilder().reportParameters(List.of()).build();

            ReportReports actual = reportEntityMapper.toDto(entity);

            assertTrue(actual.getReportParameters() == null || actual.getReportParameters().isEmpty());
        }

    }

    @Nested
    @DisplayName("toDto() - Supported File Types Handling")
    class SupportedFileTypesHandling {

        @Test
        @DisplayName("Should return empty list when supported file types is empty")
        void toDto_withEmptySupportedFileTypes_shouldReturnEmptyList() {
            ReportEntity entity = defaultReportEntityBuilder().supportedFileTypes(List.of()).build();

            assertTrue(reportEntityMapper.toDto(entity).getSupportedFileTypes().isEmpty());
        }

        @Test
        @DisplayName("Should return null when supported file types is null")
        void toDto_withNullSupportedFileTypes_shouldReturnNull() {
            ReportEntity entity = createMinimalReportEntity();

            List<?> result = reportEntityMapper.toDto(entity).getSupportedFileTypes();

            assertTrue(result == null || result.isEmpty());
        }

        @Test
        @DisplayName("Should map all three supported file type enums correctly")
        void toDto_withAllFileTypes_shouldMapAllTypes() {
            ReportEntity entity = defaultReportEntityBuilder().supportedFileTypes(Arrays.asList(CSV, PDF, XML)).build();

            List<ReportReports.SupportedFileTypesEnum> types = reportEntityMapper.toDto(entity).getSupportedFileTypes();

            assertAll(
                () -> assertEquals(3, types.size()),
                () -> assertEquals(ReportReports.SupportedFileTypesEnum.CSV, types.getFirst()),
                () -> assertEquals(ReportReports.SupportedFileTypesEnum.PDF, types.get(1)),
                () -> assertEquals(ReportReports.SupportedFileTypesEnum.XML, types.get(2))
            );
        }
    }

    @Nested
    @DisplayName("toDto() - Retention Period Handling")
    class RetentionPeriodHandling {

        @ParameterizedTest(name = "{0} -> {1}")
        @DisplayName("Should map ISO 8601 duration retention period correctly")
        @CsvSource(nullValues = "NULL", value = {
            "NULL,        NULL",         // null retention period
            "PT0S,        PT0S",         // zero / empty duration
            "P3DT4H5M6S, PT76H5M6S",   // days + hours + minutes + seconds
            "PT4H5M6S,   PT4H5M6S",    // hours + minutes + seconds only
            "P3DT0H0M0S, PT72H",        // days only (no time components)
            "PT0H5M6S,   PT5M6S",       // minutes + seconds only
            "PT0H0M6S,   PT6S",         // seconds only
            "PT4H0M0S,   PT4H",         // hours only
            "P1DT1H1M1S, PT25H1M1S"    // 1 day + 1 hour + 1 min + 1 sec
        })
        void toDto_withVariousDurationCombinations_shouldMapCorrectly(String input, String expected) {
            Duration duration = input != null ? Duration.parse(input) : null;
            ReportEntity entity = defaultReportEntityBuilder().retentionPeriod(duration).build();

            assertEquals(expected, reportEntityMapper.toDto(entity).getRetentionPeriod());
        }

    }
}
