package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TransactionTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(TransactionType.DISHONOURED_CHEQUE, TransactionType.getByLabel("DISHCQ"));
        assertEquals(TransactionType.TRANSFER_FROM_SUSPENSE, TransactionType.getByLabel("FR_SUS"));
        assertEquals(TransactionType.MONIES_MANUALLY_ADJUSTED_TO_SUSPENSE, TransactionType.getByLabel("MADJ"));
        assertEquals(TransactionType.PAYMENT, TransactionType.getByLabel("PAYMNT"));
        assertEquals(TransactionType.REVERSED_PAYMENT, TransactionType.getByLabel("REVPAY"));
        assertEquals(TransactionType.REVERSED_WRITE_OFF, TransactionType.getByLabel("RVWOFF"));
        assertEquals(TransactionType.TRANSFERRED_OUT, TransactionType.getByLabel("TFO"));
        assertEquals(TransactionType.TRANSFERRED_IN, TransactionType.getByLabel("TFO_IN"));
        assertEquals(TransactionType.WRITE_OFF, TransactionType.getByLabel("WRTOFF"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.getByLabel("unknown"));
    }
}
