package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.Priority;

public class PriorityConverterTest {

    private final PriorityConverter converter = new PriorityConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        String dbValueZero = converter.convertToDatabaseColumn(Priority.ZERO);
        String dbValueOne = converter.convertToDatabaseColumn(Priority.ONE);
        String dbValueTwo = converter.convertToDatabaseColumn(Priority.TWO);

        assertEquals("0", dbValueZero);
        assertEquals("1", dbValueOne);
        assertEquals("2", dbValueTwo);
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        Priority priorityZero = converter.convertToEntityAttribute("0");
        Priority priorityOne = converter.convertToEntityAttribute("1");
        Priority priorityTwo = converter.convertToEntityAttribute("2");

        assertEquals(Priority.ZERO, priorityZero);
        assertEquals(Priority.ONE, priorityOne);
        assertEquals(Priority.TWO, priorityTwo);
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
