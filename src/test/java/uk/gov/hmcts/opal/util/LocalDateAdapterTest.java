package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateAdapterTest {

    private final LocalDateAdapter adapter = new LocalDateAdapter();

    @Test
    void shouldUnmarshalValidStringToLocalDate() throws Exception {
        LocalDate result = adapter.unmarshal("2022-04-01");
        assertEquals(LocalDate.of(2022, Month.APRIL, 1), result);
    }

    @Test
    void shouldThrowExceptionWhenUnmarshalInvalidString() {
        assertThrows(Exception.class, () -> adapter.unmarshal("invalid"));
    }

    @Test
    void shouldMarshalLocalDateToValidString() throws Exception {
        String result = adapter.marshal(LocalDate.of(2022, Month.APRIL, 1));
        assertEquals("2022-04-01", result);
    }
}
