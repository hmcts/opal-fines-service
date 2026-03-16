package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class AccountTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(AccountType.DEFENDANT, AccountType.getByLabel("Defendant"));
        assertEquals(AccountType.CREDITOR, AccountType.getByLabel("Creditor"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> AccountType.getByLabel("Unknown"));
    }
}
