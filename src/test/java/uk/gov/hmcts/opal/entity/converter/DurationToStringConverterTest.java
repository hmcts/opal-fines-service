package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DurationToStringConverterTest {

    private final DurationToStringConverter converter = new DurationToStringConverter();

    @Test
    void shouldReturnNull_whenInputIsNull() {
        assertAll(
            () -> assertNull(converter.convertToDatabaseColumn(null)),
            () -> assertNull(converter.convertToEntityAttribute(null))
        );
    }

    @ParameterizedTest
    @MethodSource("durationStringCases")
    void shouldConvertBothDirections(Duration duration, String isoString) {
        assertAll(
            () -> assertEquals(isoString, converter.convertToDatabaseColumn(duration)),
            () -> assertEquals(duration,  converter.convertToEntityAttribute(isoString))
        );
    }

    static Stream<Arguments> durationStringCases() {
        return Stream.of(
            Arguments.of(Duration.ofDays(14), "PT336H"),
            Arguments.of(Duration.ofHours(2), "PT2H"),
            Arguments.of(Duration.ZERO,       "PT0S")
        );
    }
}