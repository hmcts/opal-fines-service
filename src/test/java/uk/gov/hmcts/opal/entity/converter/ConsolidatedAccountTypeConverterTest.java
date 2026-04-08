package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountType;

class ConsolidatedAccountTypeConverterTest {

    private final ConsolidatedAccountTypeConverter converter = new ConsolidatedAccountTypeConverter();

    @Test
    void convertToDatabaseColumn_known() {
        assertEquals("M", converter.convertToDatabaseColumn(ConsolidatedAccountType.MASTER));
        assertEquals("C", converter.convertToDatabaseColumn(ConsolidatedAccountType.CHILD));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(ConsolidatedAccountType.MASTER, converter.convertToEntityAttribute("M"));
        assertEquals(ConsolidatedAccountType.CHILD, converter.convertToEntityAttribute("C"));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("X"));
    }
}
