package uk.gov.hmcts.opal.entity.converter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeExtended;


public class EnforcementAccountTypeExtendedConverterTest {

    private EnforcementAccountTypeExtendedConverter converter = new EnforcementAccountTypeExtendedConverter();


    @ParameterizedTest(name = "#{index} enum value: {0}")
    @EnumSource(EnforcementAccountTypeExtended.class)
    void convertToDatabaseColumn(EnforcementAccountTypeExtended enforcementAccountType) {
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
        EnforcementAccountTypeExtended colh = converter.convertToEntityAttribute("COLH");
        EnforcementAccountTypeExtended coll = converter.convertToEntityAttribute("COLL");
        EnforcementAccountTypeExtended fpvh = converter.convertToEntityAttribute("FPVH");
        EnforcementAccountTypeExtended fpvl = converter.convertToEntityAttribute("FPVL");

        EnforcementAccountTypeExtended nullAttr = converter.convertToEntityAttribute(null);

        assertAll(
            () -> assertEquals(EnforcementAccountTypeExtended.ADULT_COLLECTION_ORDER_HIGH, colh),
            () -> assertEquals(EnforcementAccountTypeExtended.ADULT_COLLECTION_ORDER_LOW, coll),
            () -> assertEquals(EnforcementAccountTypeExtended.FPVH, fpvh),
            () -> assertEquals(EnforcementAccountTypeExtended.FPVL, fpvl),
            () -> assertNull(nullAttr)
        );

    }

}
