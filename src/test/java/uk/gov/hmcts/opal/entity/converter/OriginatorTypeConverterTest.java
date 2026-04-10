package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;

class OriginatorTypeConverterTest {

    private final OriginatorTypeConverter converter = new OriginatorTypeConverter();

    @Test
    void convertToDatabaseColumn_known() {
        assertEquals("NEW", converter.convertToDatabaseColumn(OriginatorType.MAC_NEW_ACCOUNT));
        assertEquals("FP", converter.convertToDatabaseColumn(OriginatorType.FIXED_PENALTY));
        assertEquals("TFO", converter.convertToDatabaseColumn(OriginatorType.TRANSFER_IN_ACCOUNT));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(OriginatorType.MAC_NEW_ACCOUNT, converter.convertToEntityAttribute("NEW"));
        assertEquals(OriginatorType.FIXED_PENALTY, converter.convertToEntityAttribute("FP"));
        assertEquals(OriginatorType.TRANSFER_IN_ACCOUNT, converter.convertToEntityAttribute("TFO"));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("MANUAL"));
    }
}
