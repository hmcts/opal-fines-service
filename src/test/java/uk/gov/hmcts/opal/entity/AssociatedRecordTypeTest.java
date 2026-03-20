package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AssociatedRecordTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(AssociatedRecordType.DEFENDANT_ACCOUNTS,
            AssociatedRecordType.getByLabel("defendant_accounts"));
        assertEquals(AssociatedRecordType.CREDITOR_TRANSACTIONS,
            AssociatedRecordType.getByLabel("creditor_transactions"));
        assertEquals(AssociatedRecordType.IMPOSITIONS,
            AssociatedRecordType.getByLabel("impositions"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> AssociatedRecordType.getByLabel("unknown"));
    }
}
