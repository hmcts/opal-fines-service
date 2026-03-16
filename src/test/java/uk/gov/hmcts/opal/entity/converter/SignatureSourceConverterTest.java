package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.SignatureSource;

class SignatureSourceConverterTest {

    private final SignatureSourceConverter converter = new SignatureSourceConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        String dbValueArea = converter.convertToDatabaseColumn(SignatureSource.AREA);
        String dbValueLja = converter.convertToDatabaseColumn(SignatureSource.LJA);

        assertEquals("Area", dbValueArea);
        assertEquals("LJA", dbValueLja);
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        SignatureSource signatureSourceLja = converter.convertToEntityAttribute("LJA");
        SignatureSource signatureSourceArea = converter.convertToEntityAttribute("Area");

        assertEquals(SignatureSource.AREA, signatureSourceArea);
        assertEquals(SignatureSource.LJA, signatureSourceLja);
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