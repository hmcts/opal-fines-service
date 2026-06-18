package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;

class DefendantTransactionStatusConverterTest {

    private final DefendantTransactionStatusConverter converter = new DefendantTransactionStatusConverter();

    @Test
    void convertToDatabaseColumn_known() {
        assertEquals("C", converter.convertToDatabaseColumn(DefendantTransactionStatus.CLEARED_PRESENTED));
        assertEquals("D", converter.convertToDatabaseColumn(DefendantTransactionStatus.DISHONOURED));
        assertEquals("P", converter.convertToDatabaseColumn(DefendantTransactionStatus.PARTIALLY_REVERSED));
        assertEquals("R", converter.convertToDatabaseColumn(DefendantTransactionStatus.REVERSED));
        assertEquals("X", converter.convertToDatabaseColumn(DefendantTransactionStatus.CANCELLED));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(DefendantTransactionStatus.CLEARED_PRESENTED, converter.convertToEntityAttribute("C"));
        assertEquals(DefendantTransactionStatus.DISHONOURED, converter.convertToEntityAttribute("D"));
        assertEquals(DefendantTransactionStatus.PARTIALLY_REVERSED, converter.convertToEntityAttribute("P"));
        assertEquals(DefendantTransactionStatus.REVERSED, converter.convertToEntityAttribute("R"));
        assertEquals(DefendantTransactionStatus.CANCELLED, converter.convertToEntityAttribute("X"));
    }

    @Test
    void convertToEntityAttribute_blank_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("UNKNOWN"));
    }

}