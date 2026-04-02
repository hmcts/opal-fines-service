package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

public class DraftAccountTypeConverterTest {

    private final DraftAccountTypeConverter converter = new DraftAccountTypeConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        assertEquals("Fine", converter.convertToDatabaseColumn(DraftAccountType.FINE));
        assertEquals("Fixed Penalty", converter.convertToDatabaseColumn(DraftAccountType.FIXED_PENALTY));
        assertEquals("Conditional Caution", converter.convertToDatabaseColumn(DraftAccountType.CONDITIONAL_CAUTION));
        assertEquals("Confiscation", converter.convertToDatabaseColumn(DraftAccountType.CONFISCATION));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(DraftAccountType.FINE, converter.convertToEntityAttribute("Fine"));
        assertEquals(DraftAccountType.FIXED_PENALTY, converter.convertToEntityAttribute("Fixed Penalty"));
        assertEquals(DraftAccountType.CONDITIONAL_CAUTION,
                     converter.convertToEntityAttribute("Conditional Caution"));
        assertEquals(DraftAccountType.CONFISCATION, converter.convertToEntityAttribute("Confiscation"));
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
