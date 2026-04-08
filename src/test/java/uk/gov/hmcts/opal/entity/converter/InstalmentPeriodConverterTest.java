package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;

public class InstalmentPeriodConverterTest {

    private final InstalmentPeriodConverter converter = new InstalmentPeriodConverter();

    @Test
    void convertToDatabaseColumn_nonNull() {
        String dbValueFortnight = converter.convertToDatabaseColumn(InstalmentPeriod.FORTNIGHT);
        String dbValueMonth = converter.convertToDatabaseColumn(InstalmentPeriod.MONTH);
        String dbValueWeek = converter.convertToDatabaseColumn(InstalmentPeriod.WEEK);

        assertEquals("F", dbValueFortnight);
        assertEquals("M", dbValueMonth);
        assertEquals("W", dbValueWeek);
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_known() {
        InstalmentPeriod fortnight = converter.convertToEntityAttribute("F");
        InstalmentPeriod month = converter.convertToEntityAttribute("M");
        InstalmentPeriod week = converter.convertToEntityAttribute("W");

        assertEquals(InstalmentPeriod.FORTNIGHT, fortnight);
        assertEquals(InstalmentPeriod.MONTH, month);
        assertEquals(InstalmentPeriod.WEEK, week);
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_blank() {
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void convertToEntityAttribute_unknown() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
