package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
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
        try (MockedStatic<LowHighValue> lowHighValue = mockStatic(LowHighValue.class)) {
            converter.convertToEntityAttribute("L");
            lowHighValue.verify(() -> LowHighValue.getByValue("L"), times(1));
        }
    }

    @Test
    void convertToNullEntityAttribute() {
        try (MockedStatic<LowHighValue> lowHighValue = mockStatic(LowHighValue.class)) {
            LowHighValue entityAttr = converter.convertToEntityAttribute(null);

            assertNull(entityAttr);
            lowHighValue.verifyNoInteractions();
        }
    }

}
