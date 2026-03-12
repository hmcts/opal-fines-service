package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessUnitTypeConverterTest {

    private final BusinessUnitTypeConverter converter = new BusinessUnitTypeConverter();

    @Test
    void given_businessUnitType_when_convertToDatabaseColumn_then_returnsLabel() {
        assertEquals("Accounting Division", converter.convertToDatabaseColumn(BusinessUnitType.ACCOUNTING_DIVISION));
        assertEquals("Area", converter.convertToDatabaseColumn(BusinessUnitType.AREA));
    }

    @Test
    void given_nullBusinessUnitType_when_convertToDatabaseColumn_then_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void given_validLabel_when_convertToEntityAttribute_then_returnsBusinessUnitType() {
        assertEquals(BusinessUnitType.ACCOUNTING_DIVISION,
                     converter.convertToEntityAttribute("Accounting Division"));
        assertEquals(BusinessUnitType.AREA, converter.convertToEntityAttribute("Area"));
    }

    @Test
    void given_blankOrNullLabel_when_convertToEntityAttribute_then_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(" "));
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void given_unknownLabel_when_convertToEntityAttribute_then_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Unknown"));
    }
}
