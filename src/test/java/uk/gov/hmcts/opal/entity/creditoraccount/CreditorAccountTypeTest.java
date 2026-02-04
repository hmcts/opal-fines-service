package uk.gov.hmcts.opal.entity.creditoraccount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditorAccountTypeTest {

    @Test
    void given_enumValues_when_checkTypeHelpers_then_expectedResults() {
        assertTrue(CreditorAccountType.MN.isMinorCreditor());
        assertFalse(CreditorAccountType.MN.isMajorCreditor());
        assertFalse(CreditorAccountType.MN.isCentralFund());

        assertTrue(CreditorAccountType.MJ.isMajorCreditor());
        assertFalse(CreditorAccountType.MJ.isMinorCreditor());
        assertFalse(CreditorAccountType.MJ.isCentralFund());

        assertTrue(CreditorAccountType.CF.isCentralFund());
        assertFalse(CreditorAccountType.CF.isMinorCreditor());
        assertFalse(CreditorAccountType.CF.isMajorCreditor());
    }

    @Test
    void given_validCode_when_getDisplayName_then_labelReturned() {
        assertEquals("Minor Creditor", CreditorAccountType.getDisplayName("MN"));
        assertEquals("Major Creditor", CreditorAccountType.getDisplayName("MJ"));
        assertEquals("Central Fund", CreditorAccountType.getDisplayName("CF"));
    }

    @Test
    void given_invalidOrNullCode_when_getDisplayName_then_nullReturned() {
        assertNull(CreditorAccountType.getDisplayName("UNKNOWN"));
        assertNull(CreditorAccountType.getDisplayName(null));
    }
}
