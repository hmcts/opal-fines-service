package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ChequeStatusTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(ChequeStatusType.DESTROYED, ChequeStatusType.getByLabel("D"));
        assertEquals(ChequeStatusType.NEW, ChequeStatusType.getByLabel("N"));
        assertEquals(ChequeStatusType.PRESENTED, ChequeStatusType.getByLabel("P"));
        assertEquals(ChequeStatusType.QUERY, ChequeStatusType.getByLabel("Q"));
        assertEquals(ChequeStatusType.WITHDRAWN, ChequeStatusType.getByLabel("W"));
        assertEquals(ChequeStatusType.AWAITING_DELETION, ChequeStatusType.getByLabel("X"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> ChequeStatusType.getByLabel("unknown"));
    }
}
