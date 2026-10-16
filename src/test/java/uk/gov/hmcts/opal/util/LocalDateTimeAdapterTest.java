package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LocalDateTimeAdapterTest {

    private final LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

    @ParameterizedTest
    @CsvSource({
        "2022-04-01T12:00:00, 2022-04-01T12:00:00",
        "2022-04-01T12:00:00.123456789, 2022-04-01T12:00:00.123456789",
        "2022-04-01T12:00, 2022-04-01T12:00:00",
        "2022-04-01 12:00:00, 2022-04-01T12:00:00",
        "2022-04-01 12:00:00.00000, 2022-04-01T12:00:00",
        "2022-04-01 12:00:00.123456789, 2022-04-01T12:00:00.123456789",
        "2022-04-01, 2022-04-01T00:00:00"
    })
    void shouldUnmarshalValidStringToLocalDateTime(String value, LocalDateTime expected) throws Exception {
        assertEquals(expected, adapter.unmarshal(value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldUnmarshalBlankValueToNull(String value) throws Exception {
        assertNull(adapter.unmarshal(value));
    }

    @Test
    void shouldThrowExceptionWhenUnmarshalInvalidString() {
        assertThrows(Exception.class, () -> adapter.unmarshal("invalid"));
    }

    @Test
    void shouldMarshalLocalDateTimeToValidString() throws Exception {
        String result = adapter.marshal(LocalDateTime.of(2022, Month.APRIL, 1, 12, 0));
        assertEquals("2022-04-01T12:00", result);
    }
}
