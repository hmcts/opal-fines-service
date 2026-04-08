package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;

class DefendantAccountStatusConverterTest {

    private final DefendantAccountStatusConverter converter = new DefendantAccountStatusConverter();

    @Test
    void convertToDatabaseColumn_known() {
        assertEquals("CS", converter.convertToDatabaseColumn(DefendantAccountStatus.ACCOUNT_CONSOLIDATED));
        assertEquals("L", converter.convertToDatabaseColumn(DefendantAccountStatus.LIVE));
        assertEquals("TA", converter.convertToDatabaseColumn(DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED));
        assertEquals("TO", converter.convertToDatabaseColumn(DefendantAccountStatus.TRANSFER_OUT_PENDING));
        assertEquals("TS", converter.convertToDatabaseColumn(DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND));
        assertEquals("WO", converter.convertToDatabaseColumn(DefendantAccountStatus.ACCOUNT_WRITTEN_OFF));
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        assertEquals(DefendantAccountStatus.ACCOUNT_CONSOLIDATED, converter.convertToEntityAttribute("CS"));
        assertEquals(DefendantAccountStatus.LIVE, converter.convertToEntityAttribute("L"));
        assertEquals(DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED, converter.convertToEntityAttribute("TA"));
        assertEquals(DefendantAccountStatus.TRANSFER_OUT_PENDING, converter.convertToEntityAttribute("TO"));
        assertEquals(DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND, converter.convertToEntityAttribute("TS"));
        assertEquals(DefendantAccountStatus.ACCOUNT_WRITTEN_OFF, converter.convertToEntityAttribute("WO"));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("XX"));
    }
}
