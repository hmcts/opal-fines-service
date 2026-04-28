package uk.gov.hmcts.opal.mapper.helper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for DurationMapperHelper.
 *
 * @author Krishna Sapkota
 */
@DisplayName("DurationMapperHelper Tests [@PO-2250]")
class DurationMapperHelperTest {

    private DurationMapperHelper cut;

    @BeforeEach
    void setUp() {
        cut = new DurationMapperHelper();
    }

    @Nested
    @DisplayName("durationToString() - Null Cases")
    class DurationToStringNullCases {

        @Test
        @DisplayName("Should return null when duration is null")
        void durationToString_withNullDuration_shouldReturnNull() {
            Duration duration = null;

            String actual = cut.durationToString(duration);

            assertNull(actual, "Result should be null when duration is null");
        }
    }

    @Nested
    @DisplayName("durationToString() - Period Format Cases")
    class DurationToStringPeriodFormatCases {

        @ParameterizedTest(name = "Should convert {0} days to {1}")
        @CsvSource({
            "0, P0D",
            "1, P1D",
            "15, P15D",
            "30, P30D",
            "365, P365D"
        })
        @DisplayName("Should convert whole days to Period format (P{n}D)")
        void durationToString_withWholeDays_shouldReturnPeriodFormat(long days, String expected) {
            Duration duration = Duration.ofDays(days);

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual,
                String.format("Should convert %d days to %s format", days, expected));
        }
    }

    @Nested
    @DisplayName("durationToString() - Duration Format Cases")
    class DurationToStringDurationFormatCases {

        @Test
        @DisplayName("Should convert hours to Duration format")
        void durationToString_withHours_shouldReturnDurationFormat() {
            Duration duration = Duration.ofHours(5);
            String expected = "PT5H";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 5 hours to PT5H format");
        }

        @Test
        @DisplayName("Should convert hours and minutes to Duration format")
        void durationToString_withHoursAndMinutes_shouldReturnDurationFormat() {
            Duration duration = Duration.ofHours(5).plusMinutes(30);
            String expected = "PT5H30M";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 5 hours 30 minutes to PT5H30M format");
        }

        @Test
        @DisplayName("Should convert complex duration to Duration format")
        void durationToString_withComplexDuration_shouldReturnDurationFormat() {
            Duration duration = Duration.ofHours(2).plusMinutes(45).plusSeconds(30);
            String expected = "PT2H45M30S";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 2h 45m 30s to PT2H45M30S format");
        }

        @Test
        @DisplayName("Should convert minutes to Duration format")
        void durationToString_withMinutes_shouldReturnDurationFormat() {
            Duration duration = Duration.ofMinutes(90);
            String expected = "PT1H30M";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 90 minutes to PT1H30M format");
        }

        @Test
        @DisplayName("Should convert seconds to Duration format")
        void durationToString_withSeconds_shouldReturnDurationFormat() {
            Duration duration = Duration.ofSeconds(90);
            String expected = "PT1M30S";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 90 seconds to PT1M30S format");
        }

        @Test
        @DisplayName("Should convert partial day to Duration format (not Period)")
        void durationToString_withPartialDay_shouldReturnDurationFormat() {
            Duration duration = Duration.ofHours(25); // 1 day + 1 hour
            String expected = "PT25H";

            String actual = cut.durationToString(duration);

            assertEquals(expected, actual, "Should convert 25 hours to PT25H format (not P1D)");
        }
    }

    @Nested
    @DisplayName("stringToDuration() - Null/Empty Cases")
    class StringToDurationNullEmptyCases {

        @ParameterizedTest(name = "Should return null when string is {0}")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "  "})
        @DisplayName("Should return null for null, empty, or whitespace strings")
        void stringToDuration_withNullOrEmptyString_shouldReturnNull(String input) {
            Duration actual = cut.stringToDuration(input);

            assertNull(actual, "Result should be null when string is null, empty, or whitespace");
        }
    }

    @Nested
    @DisplayName("stringToDuration() - Period Format Cases")
    class StringToDurationPeriodFormatCases {

        @ParameterizedTest(name = "Should parse {0} to {1} days")
        @CsvSource({
            "P0D, 0",
            "P1D, 1",
            "P15D, 15",
            "P30D, 30",
            "P365D, 365"
        })
        @DisplayName("Should parse Period format (P{n}D) to Duration")
        void stringToDuration_withPeriodFormat_shouldParseToDuration(String input, long expectedDays) {
            Duration expected = Duration.ofDays(expectedDays);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify Period format parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual,
                    String.format("Should parse %s to %d days", input, expectedDays))
            );
        }
    }

    @Nested
    @DisplayName("stringToDuration() - Duration Format Cases")
    class StringToDurationDurationFormatCases {

        @Test
        @DisplayName("Should parse PT5H to 5 hours")
        void stringToDuration_withHours_shouldParseToDuration() {
            String input = "PT5H";
            Duration expected = Duration.ofHours(5);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify hours parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual, "Should parse PT5H to 5 hours")
            );
        }

        @Test
        @DisplayName("Should parse PT5H30M to 5 hours 30 minutes")
        void stringToDuration_withHoursAndMinutes_shouldParseToDuration() {
            String input = "PT5H30M";
            Duration expected = Duration.ofHours(5).plusMinutes(30);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify hours and minutes parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual, "Should parse PT5H30M to 5 hours 30 minutes")
            );
        }

        @Test
        @DisplayName("Should parse PT2H45M30S to complex duration")
        void stringToDuration_withComplexFormat_shouldParseToDuration() {
            String input = "PT2H45M30S";
            Duration expected = Duration.ofHours(2).plusMinutes(45).plusSeconds(30);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify complex duration parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual,
                    "Should parse PT2H45M30S to 2 hours 45 minutes 30 seconds")
            );
        }

        @Test
        @DisplayName("Should parse PT1H30M to 90 minutes")
        void stringToDuration_with90Minutes_shouldParseToDuration() {
            String input = "PT1H30M";
            Duration expected = Duration.ofMinutes(90);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify 90 minutes parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual, "Should parse PT1H30M to 90 minutes")
            );
        }

        @Test
        @DisplayName("Should parse PT720H to 720 hours")
        void stringToDuration_with720Hours_shouldParseToDuration() {
            String input = "PT720H";
            Duration expected = Duration.ofHours(720);

            Duration actual = cut.stringToDuration(input);

            assertAll("Verify 720 hours parsing",
                () -> assertNotNull(actual, "Result should not be null"),
                () -> assertEquals(expected, actual, "Should parse PT720H to 720 hours")
            );
        }
    }

    @Nested
    @DisplayName("stringToDuration() - Error Cases")
    class StringToDurationErrorCases {

        @ParameterizedTest(name = "Should throw exception for invalid format: {0}")
        @ValueSource(strings = {
            "INVALID",
            "P30X",
            "PT",
            "P",
            "XYZ"
        })
        @DisplayName("Should throw IllegalArgumentException for invalid formats")
        void stringToDuration_withInvalidFormat_shouldThrowException(String invalidInput) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cut.stringToDuration(invalidInput),
                "Should throw IllegalArgumentException for invalid format"
            );

            assertEquals("Invalid duration format: " + invalidInput, exception.getMessage(),
                "Exception message should indicate invalid format");
        }
    }

    @Nested
    @DisplayName("Round-trip Conversion Tests")
    class RoundTripConversionTests {

        @ParameterizedTest(name = "Should preserve {0} days in round-trip conversion")
        @ValueSource(longs = {0, 1, 15, 30, 365})
        @DisplayName("Should handle round-trip conversion for whole days")
        void roundTrip_withWholeDays_shouldPreserveValue(long days) {
            Duration original = Duration.ofDays(days);
            String expectedString = "P" + days + "D";

            String stringValue = cut.durationToString(original);
            Duration actual = cut.stringToDuration(stringValue);

            assertAll("Verify round-trip conversion for " + days + " days",
                () -> assertEquals(expectedString, stringValue,
                    "String representation should be " + expectedString),
                () -> assertEquals(original, actual,
                    "Round-trip conversion should preserve " + days + " days")
            );
        }

        @Test
        @DisplayName("Should handle round-trip conversion for hours")
        void roundTrip_withHours_shouldPreserveValue() {
            Duration original = Duration.ofHours(5);
            String expectedString = "PT5H";

            String stringValue = cut.durationToString(original);
            Duration actual = cut.stringToDuration(stringValue);

            assertAll("Verify round-trip conversion for hours",
                () -> assertEquals(expectedString, stringValue,
                    "String representation should be PT5H"),
                () -> assertEquals(original, actual,
                    "Round-trip conversion should preserve 5 hours")
            );
        }

        @Test
        @DisplayName("Should handle round-trip conversion for complex duration")
        void roundTrip_withComplexDuration_shouldPreserveValue() {
            Duration original = Duration.ofHours(2).plusMinutes(45).plusSeconds(30);
            String expectedString = "PT2H45M30S";

            String stringValue = cut.durationToString(original);
            Duration actual = cut.stringToDuration(stringValue);

            assertAll("Verify round-trip conversion for complex duration",
                () -> assertEquals(expectedString, stringValue,
                    "String representation should be PT2H45M30S"),
                () -> assertEquals(original, actual,
                    "Round-trip conversion should preserve 2h 45m 30s")
            );
        }
    }
}