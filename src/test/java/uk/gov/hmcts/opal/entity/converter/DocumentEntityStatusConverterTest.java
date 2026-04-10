package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.document.DocumentEntityStatus;

class DocumentEntityStatusConverterTest {

    private final DocumentEntityStatusConverter converter = new DocumentEntityStatusConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        assertEquals("New", converter.convertToDatabaseColumn(DocumentEntityStatus.NEW));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(DocumentEntityStatus.NEW, converter.convertToEntityAttribute("New"));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute("UnknownStatus"));
    }
}
