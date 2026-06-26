package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountType;


public class EnforcementAccountTypeConverterTest {

    private EnforcementAccountTypeConverter converter = new EnforcementAccountTypeConverter();

    @ParameterizedTest(name = "#{index} enum value: {0}")
    @EnumSource(EnforcementAccountType.class)
    void convertToDatabaseColumn(EnforcementAccountType enforcementAccountType) {
        String value = converter.convertToDatabaseColumn(enforcementAccountType);

        assertEquals(enforcementAccountType.getCode(), value);
    }

    @Test
    void convertNullToDatabaseColumn() {
        String value = converter.convertToDatabaseColumn(null);

        assertNull(value);
    }

    @Test
    void convertToEntityAttribute() {
        try (MockedStatic<EnforcementAccountType> enfAccountTyoe = mockStatic(EnforcementAccountType.class)) {
            converter.convertToEntityAttribute("COLH");
            enfAccountTyoe.verify(() -> EnforcementAccountType.getByCode("COLH"), times(1));
        }
    }

    @Test
    void convertToNullEntityAttribute() {
        try (MockedStatic<EnforcementAccountType> enfAccountTyoe = mockStatic(EnforcementAccountType.class)) {
            EnforcementAccountType entityAttr = converter.convertToEntityAttribute(null);

            assertNull(entityAttr);
            enfAccountTyoe.verifyNoInteractions();
        }
    }

}
