package uk.gov.hmcts.opal.entity.converter;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountType;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountTypeConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefendantAccountTypeConverterTest {

    @Test
    public void testConversions() {

        DefendantAccountTypeConverter datc = new DefendantAccountTypeConverter();

        assertEquals("Fines",
                     datc.convertToDatabaseColumn(DefendantAccountType.FINES));
        assertEquals("Fixed Penalty",
                     datc.convertToDatabaseColumn(DefendantAccountType.FIXED_PENALTY));
        assertEquals("Confiscation",
                     datc.convertToDatabaseColumn(DefendantAccountType.CONFISCATION));
        assertEquals("Conditional Caution",
                     datc.convertToDatabaseColumn(DefendantAccountType.CONDITIONAL_CAUTION));

        assertEquals(DefendantAccountType.CONDITIONAL_CAUTION,
                     datc.convertToEntityAttribute("Conditional Caution"));
        assertEquals(DefendantAccountType.CONFISCATION,
                     datc.convertToEntityAttribute("Confiscation"));
        assertEquals(DefendantAccountType.FIXED_PENALTY,
                     datc.convertToEntityAttribute("Fixed Penalty"));
        assertEquals(DefendantAccountType.FINES,
                     datc.convertToEntityAttribute("Fines"));

    }

}
