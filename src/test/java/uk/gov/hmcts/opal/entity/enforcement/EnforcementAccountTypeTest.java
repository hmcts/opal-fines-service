package uk.gov.hmcts.opal.entity.enforcement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EnforcementAccountTypeTest {

    @Test
    void getByCode() {
        EnforcementAccountType colLow = EnforcementAccountType.getByCode("COLL");
        EnforcementAccountType youthLow = EnforcementAccountType.getByCode("YL");

        assertAll(
            () -> assertEquals(EnforcementAccountType.ADULT_COLLECTION_ORDER_LOW, colLow),
            () -> assertEquals(EnforcementAccountType.YOUTH_LOW, youthLow)
        );
    }

    @Test
    void getByCode_Unknown_Throws() {
        assertThrows(IllegalArgumentException.class, () -> EnforcementAccountType.getByCode("foo"));
    }
}
