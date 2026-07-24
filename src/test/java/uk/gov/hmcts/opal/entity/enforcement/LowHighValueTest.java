package uk.gov.hmcts.opal.entity.enforcement;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.LowHighValue;

public class LowHighValueTest {

    @Test
    void getByCode() {
        LowHighValue low = LowHighValue.getByValue("L");
        LowHighValue high = LowHighValue.getByValue("H");

        assertAll(
            () -> assertEquals(LowHighValue.LOW, low),
            () -> assertEquals(LowHighValue.HIGH, high)
        );
    }

    @Test
    void getByCode_Unknown_Throws() {
        assertThrows(IllegalArgumentException.class, () -> LowHighValue.getByValue("foo"));
    }
}
