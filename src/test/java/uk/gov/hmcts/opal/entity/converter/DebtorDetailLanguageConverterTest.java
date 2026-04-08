package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.debtordetail.Language;

class DebtorDetailLanguageConverterTest {

    private final DebtorDetailLanguageConverter converter = new DebtorDetailLanguageConverter();

    @Test
    void convertToDatabaseColumn_known() {
        assertEquals("EN", converter.convertToDatabaseColumn(Language.ENGLISH));
        assertEquals("CY", converter.convertToDatabaseColumn(Language.WELSH_AND_ENGLISH));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(Language.ENGLISH, converter.convertToEntityAttribute("EN"));
        assertEquals(Language.WELSH_AND_ENGLISH, converter.convertToEntityAttribute("CY"));
    }

    @Test
    void convertToEntityAttribute_blank_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("FR"));
    }
}
