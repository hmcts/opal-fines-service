package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalDateTimeAdapterTest {

    private final LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

    @Test
    void shouldUnmarshalValidStringToLocalDateTime() throws Exception {
        LocalDateTime result = adapter.unmarshal("2022-04-01T12:00:00");
        assertEquals(LocalDateTime.of(2022, 4, 1, 12, 0), result);
    }

    @Test
    void shouldThrowExceptionWhenUnmarshalInvalidString() {
        assertThrows(Exception.class, () -> adapter.unmarshal("invalid"));
    }

    @Test
    void shouldMarshalLocalDateTimeToValidString() throws Exception {
        String result = adapter.marshal(LocalDateTime.of(2022, 4, 1, 12, 0));
        assertEquals("2022-04-01T12:00", result);
    }
}
