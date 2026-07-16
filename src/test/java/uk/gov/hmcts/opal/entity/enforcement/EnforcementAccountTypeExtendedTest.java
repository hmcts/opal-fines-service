package uk.gov.hmcts.opal.entity.enforcement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EnforcementAccountTypeExtendedTest {

    @Test
    void getByCode() {
        EnforcementAccountTypeExtended colLow = EnforcementAccountTypeExtended.fromCode("COLL");
        EnforcementAccountTypeExtended youthLow = EnforcementAccountTypeExtended.fromCode("YL");

        assertAll(
            () -> assertEquals(EnforcementAccountTypeExtended.ADULT_COLLECTION_ORDER_LOW, colLow),
            () -> assertEquals(EnforcementAccountTypeExtended.YOUTH_LOW, youthLow)
        );
    }

    @Test
    void getByCode_Unknown_Throws() {
        assertThrows(IllegalArgumentException.class, () -> EnforcementAccountTypeExtended.fromCode("foo"));
    }
}
