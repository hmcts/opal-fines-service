package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateTimeUtilsTest {

    @ParameterizedTest
    @MethodSource("startOfProvider")
    void startOf_returnsStartOfDay(LocalDate input, LocalDateTime expected) {
        assertEquals(expected, DateTimeUtils.startOf(input));
    }

    static Stream<Arguments> startOfProvider() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(
                LocalDate.of(2024, 5, 10),
                LocalDateTime.of(2024, 5, 10, 0, 0)
            ),
            Arguments.of(
                LocalDate.of(2024, 2, 29), // leap day
                LocalDateTime.of(2024, 2, 29, 0, 0)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("endOfProvider")
    void endOf_returnsEndOfDay(LocalDate input, LocalDateTime expected) {
        assertEquals(expected, DateTimeUtils.endOf(input));
    }

    static Stream<Arguments> endOfProvider() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(
                LocalDate.of(2024, 5, 10),
                LocalDateTime.of(2024, 5, 10, 23, 59, 59, 999_999_999)
            ),
            Arguments.of(
                LocalDate.of(2024, 2, 29), // leap day
                LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_999_999)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("endOfTimeProvider")
    void endOf_usesLocalTimeMax(LocalDate input) {
        if (input == null) {
            assertNull(DateTimeUtils.endOf(null));
        } else {
            assertEquals(LocalTime.MAX, DateTimeUtils.endOf(input).toLocalTime());
        }
    }

    static Stream<LocalDate> endOfTimeProvider() {
        return Stream.of(
            null,
            LocalDate.of(2024, 1, 1)
        );
    }
}