package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BacsStatusTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(BacsStatusType.TRANSFERRED, BacsStatusType.getByLabel("T"));
        assertEquals(BacsStatusType.REISSUED, BacsStatusType.getByLabel("R"));
        assertEquals(BacsStatusType.CANCELLED_CHEQUE_WRITTEN, BacsStatusType.getByLabel("C"));
        assertEquals(BacsStatusType.CANCELLED_POSTED_TO_SUSPENSE, BacsStatusType.getByLabel("S"));
        assertEquals(BacsStatusType.CANCELLED_POSTED_TO_CENTRAL_FUND, BacsStatusType.getByLabel("F"));
        assertEquals(BacsStatusType.CLEARED_AWAITING_DELETION, BacsStatusType.getByLabel("X"));
        assertEquals(BacsStatusType.UNPROCESSED, BacsStatusType.getByLabel("U"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> BacsStatusType.getByLabel("unknown"));
    }
}
