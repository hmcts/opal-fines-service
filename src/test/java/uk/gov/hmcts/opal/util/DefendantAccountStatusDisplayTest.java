package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;

class DefendantAccountStatusDisplayTest {

    @Test
    void toDisplayName_mapsAllSupportedStatuses() {
        assertEquals("Live", DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.L));
        assertEquals("TFO to be acknowledged",
            DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.TO));
        assertEquals("TFO to NI/Scotland to be acknowledged",
            DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.TS));
        assertEquals("TFO acknowledged", DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.TA));
        assertEquals("Account consolidated", DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.CS));
        assertEquals("Account written off", DefendantAccountStatusDisplay.toDisplayName(DefendantAccountStatus.WO));
    }

    @Test
    void toDisplayName_returnsNullForNullStatus() {
        assertNull(DefendantAccountStatusDisplay.toDisplayName(null));
    }
}
