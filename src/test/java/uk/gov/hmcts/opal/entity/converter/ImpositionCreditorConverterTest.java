package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImpositionCreditorConverterTest {

    private final ImpositionCreditorConverter converter = new ImpositionCreditorConverter();

    @Test
    void given_impositionCreditor_when_convertToDatabaseColumn_then_returnsLabel() {
        assertEquals("Any", converter.convertToDatabaseColumn(ImpositionCreditor.ANY));
        assertEquals("CF", converter.convertToDatabaseColumn(ImpositionCreditor.CF));
        assertEquals("!CPS", converter.convertToDatabaseColumn(ImpositionCreditor.NOT_CPS));
        assertEquals("CPS", converter.convertToDatabaseColumn(ImpositionCreditor.CPS));
    }

    @Test
    void given_nullImpositionCreditor_when_convertToDatabaseColumn_then_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void given_validLabel_when_convertToEntityAttribute_then_returnsImpositionCreditor() {
        assertEquals(ImpositionCreditor.ANY, converter.convertToEntityAttribute("Any"));
        assertEquals(ImpositionCreditor.CF, converter.convertToEntityAttribute("CF"));
        assertEquals(ImpositionCreditor.NOT_CPS, converter.convertToEntityAttribute("!CPS"));
        assertEquals(ImpositionCreditor.CPS, converter.convertToEntityAttribute("CPS"));
    }

    @Test
    void given_blankOrNullLabel_when_convertToEntityAttribute_then_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
    }

    @Test
    void given_unknownLabel_when_convertToEntityAttribute_then_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
