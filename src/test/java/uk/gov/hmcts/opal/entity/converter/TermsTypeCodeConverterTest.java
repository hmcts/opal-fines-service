package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;

class TermsTypeCodeConverterTest {

    private final TermsTypeCodeConverter converter = new TermsTypeCodeConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        assertEquals("B", converter.convertToDatabaseColumn(TermsTypeCode.BY_DATE));
        assertEquals("I", converter.convertToDatabaseColumn(TermsTypeCode.INSTALMENTS));
        assertEquals("P", converter.convertToDatabaseColumn(TermsTypeCode.PAID));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(TermsTypeCode.BY_DATE, converter.convertToEntityAttribute("B"));
        assertEquals(TermsTypeCode.INSTALMENTS, converter.convertToEntityAttribute("I"));
        assertEquals(TermsTypeCode.PAID, converter.convertToEntityAttribute("P"));
    }

    @Test
    void convertToEntityAttribute_blank() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("X"));
    }
}
