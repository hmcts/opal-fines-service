package uk.gov.hmcts.opal.entity.draft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DraftAccountTypeTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(DraftAccountType.FINE, DraftAccountType.getByLabel("Fine"));
        assertEquals(DraftAccountType.FIXED_PENALTY, DraftAccountType.getByLabel("Fixed Penalty"));
        assertEquals(DraftAccountType.CONDITIONAL_CAUTION, DraftAccountType.getByLabel("Conditional Caution"));
        assertEquals(DraftAccountType.CONFISCATION, DraftAccountType.getByLabel("Confiscation"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> DraftAccountType.getByLabel("Unknown"));
    }
}
