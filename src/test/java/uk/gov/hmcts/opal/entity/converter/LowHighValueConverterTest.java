package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.entity.LowHighValue;


public class LowHighValueConverterTest {

    private LowHighValueConverter converter = new LowHighValueConverter();

    @ParameterizedTest(name = "#{index} enum value: {0}")
    @EnumSource(LowHighValue.class)
    void convertToDatabaseColumn(LowHighValue lowHighValue) {
        String value = converter.convertToDatabaseColumn(lowHighValue);

        assertEquals(lowHighValue.getValue(), value);
    }

    @Test
    void convertNullToDatabaseColumn() {
        String value = converter.convertToDatabaseColumn(null);

        assertNull(value);
    }

    @Test
    void convertToEntityAttribute() {
        LowHighValue low = converter.convertToEntityAttribute("L");
        LowHighValue high = converter.convertToEntityAttribute("H");
        LowHighValue nullAttr = converter.convertToEntityAttribute(null);

        assertAll(
            () -> assertEquals(LowHighValue.LOW, low),
            () -> assertEquals(LowHighValue.HIGH, high),
            () -> assertNull(nullAttr)
        );

    }

}
