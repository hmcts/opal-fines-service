package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ChequeAllocationTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(ChequeAllocationType.COMPENSATION, ChequeAllocationType.getByLabel("COMP"));
        assertEquals(ChequeAllocationType.REPAY_WITNESS_EXPENSES, ChequeAllocationType.getByLabel("REPAYW"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> ChequeAllocationType.getByLabel("unknown"));
    }
}
