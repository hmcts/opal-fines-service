package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PartyAccountTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(PartyAccountType.DEFENDANT, PartyAccountType.getByLabel("Defendant"));
        assertEquals(PartyAccountType.CREDITOR, PartyAccountType.getByLabel("Creditor"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> PartyAccountType.getByLabel("Unknown"));
    }
}
