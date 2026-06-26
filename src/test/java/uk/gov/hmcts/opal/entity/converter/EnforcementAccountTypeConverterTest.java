package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        EnforcementAccountType colh = converter.convertToEntityAttribute("COLH");
        EnforcementAccountType coll = converter.convertToEntityAttribute("COLL");
        EnforcementAccountType yh = converter.convertToEntityAttribute("YH");
        EnforcementAccountType yl = converter.convertToEntityAttribute("YL");
        EnforcementAccountType nullAttr = converter.convertToEntityAttribute(null);

        assertAll(
            () -> assertEquals(EnforcementAccountType.ADULT_COLLECTION_ORDER_HIGH, colh),
            () -> assertEquals(EnforcementAccountType.ADULT_COLLECTION_ORDER_LOW, coll),
            () -> assertEquals(EnforcementAccountType.YOUTH_HIGH, yh),
            () -> assertEquals(EnforcementAccountType.YOUTH_LOW, yl),
            () -> assertNull(nullAttr)
        );

    }

}
