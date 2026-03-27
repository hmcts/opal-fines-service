package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;

class AssociatedRecordTypeConverterTest {

    private final AssociatedRecordTypeConverter converter = new AssociatedRecordTypeConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        assertEquals("defendant_accounts", converter.convertToDatabaseColumn(AssociatedRecordType.DEFENDANT_ACCOUNTS));
        assertEquals("creditor_transactions",
            converter.convertToDatabaseColumn(AssociatedRecordType.CREDITOR_TRANSACTIONS));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(AssociatedRecordType.DEFENDANT_ACCOUNTS,
            converter.convertToEntityAttribute("defendant_accounts"));
        assertEquals(AssociatedRecordType.CREDITOR_TRANSACTIONS,
            converter.convertToEntityAttribute("creditor_transactions"));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute("unknown_record_type"));
    }
}
