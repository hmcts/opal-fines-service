package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for DurationToStringConverter.
 *
 * @author Krishna Sapkota
 */
@DisplayName("DurationToStringConverter Tests")
class DurationToStringConverterTest {

    private DurationToStringConverter cut;

    @BeforeEach
    void setUp() {
        cut = new DurationToStringConverter();
    }

    @Nested
    @DisplayName("convertToDatabaseColumn() - Duration to String")
    class ConvertToDatabaseColumn {

        @Test
        @DisplayName("Should return null when duration is null")
        void convertToDatabaseColumn_withNull_shouldReturnNull() {
            assertNull(cut.convertToDatabaseColumn(null));
        }

        @Test
        @DisplayName("Should convert whole days to Period format e.g. P14D")
        void convertToDatabaseColumn_withWholeDays_shouldReturnPeriodFormat() {
            assertEquals("P14D", cut.convertToDatabaseColumn(Duration.ofDays(14)));
        }

        @Test
        @DisplayName("Should convert 30 days to P30D")
        void convertToDatabaseColumn_with30Days_shouldReturnP30D() {
            assertEquals("P30D", cut.convertToDatabaseColumn(Duration.ofDays(30)));
        }

        @Test
        @DisplayName("Should convert 1 day to P1D")
        void convertToDatabaseColumn_with1Day_shouldReturnP1D() {
            assertEquals("P1D", cut.convertToDatabaseColumn(Duration.ofDays(1)));
        }

        @Test
        @DisplayName("Should convert hours/minutes to ISO-8601 Duration format e.g. PT5H30M")
        void convertToDatabaseColumn_withHoursAndMinutes_shouldReturnDurationFormat() {
            assertEquals("PT5H30M", cut.convertToDatabaseColumn(Duration.ofHours(5).plusMinutes(30)));
        }

        @Test
        @DisplayName("Should convert 24 hours (= 1 day) to Period format P1D")
        void convertToDatabaseColumn_withHoursOnly_shouldReturnDurationFormat() {
            assertEquals("P1D", cut.convertToDatabaseColumn(Duration.ofHours(24)));
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute() - String to Duration")
    class ConvertToEntityAttribute {

        @ParameterizedTest(name = "Should return null for [{0}]")
        @NullSource
        @ValueSource(strings = {"", "  "})
        @DisplayName("Should return null when db value is null or blank")
        void convertToEntityAttribute_withNullOrBlank_shouldReturnNull(String dbValue) {
            assertNull(cut.convertToEntityAttribute(dbValue));
        }

        @Test
        @DisplayName("Should parse P14D to Duration of 14 days")
        void convertToEntityAttribute_withP14D_shouldReturn14Days() {
            assertEquals(Duration.ofDays(14), cut.convertToEntityAttribute("P14D"));
        }

        @Test
        @DisplayName("Should parse P30D to Duration of 30 days")
        void convertToEntityAttribute_withP30D_shouldReturn30Days() {
            assertEquals(Duration.ofDays(30), cut.convertToEntityAttribute("P30D"));
        }

        @Test
        @DisplayName("Should parse P1D to Duration of 1 day")
        void convertToEntityAttribute_withP1D_shouldReturn1Day() {
            assertEquals(Duration.ofDays(1), cut.convertToEntityAttribute("P1D"));
        }

        @Test
        @DisplayName("Should parse PT5H30M to Duration of 5 hours 30 minutes")
        void convertToEntityAttribute_withPT5H30M_shouldReturn5Hours30Minutes() {
            assertEquals(Duration.ofHours(5).plusMinutes(30), cut.convertToEntityAttribute("PT5H30M"));
        }

        @Test
        @DisplayName("Should parse PT24H to Duration of 24 hours")
        void convertToEntityAttribute_withPT24H_shouldReturn24Hours() {
            assertEquals(Duration.ofHours(24), cut.convertToEntityAttribute("PT24H"));
        }

        @Test
        @DisplayName("Should parse plain numeric string '14' to Duration of 14 days")
        void convertToEntityAttribute_withPlainNumber_shouldReturn14Days() {
            assertEquals(Duration.ofDays(14), cut.convertToEntityAttribute("14"));
        }

    }

    @Nested
    @DisplayName("Round-trip conversion")
    class RoundTrip {

        @Test
        @DisplayName("Should round-trip Duration of whole days correctly")
        void roundTrip_wholeDays_shouldReturnOriginalDuration() {
            Duration original = Duration.ofDays(14);
            String dbValue = cut.convertToDatabaseColumn(original);
            Duration result = cut.convertToEntityAttribute(dbValue);
            assertAll(
                () -> assertEquals("P14D", dbValue),
                () -> assertEquals(original, result)
            );
        }

        @Test
        @DisplayName("Should round-trip Duration of hours and minutes correctly")
        void roundTrip_hoursAndMinutes_shouldReturnOriginalDuration() {
            Duration original = Duration.ofHours(5).plusMinutes(30);
            String dbValue = cut.convertToDatabaseColumn(original);
            Duration result = cut.convertToEntityAttribute(dbValue);
            assertAll(
                () -> assertEquals("PT5H30M", dbValue),
                () -> assertEquals(original, result)
            );
        }
    }
}

