package uk.gov.hmcts.opal.entity.defendantaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DefendantAccountStatusTest {

    @Test
    void getDisplayName_mapsAllSupportedStatuses() {
        assertEquals("Live", DefendantAccountStatus.LIVE.getDisplayName());
        assertEquals("TFO to be acknowledged", DefendantAccountStatus.TRANSFER_OUT_PENDING.getDisplayName());
        assertEquals("TFO to NI/Scotland to be acknowledged",
            DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND.getDisplayName());
        assertEquals("TFO acknowledged", DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED.getDisplayName());
        assertEquals("Account consolidated", DefendantAccountStatus.ACCOUNT_CONSOLIDATED.getDisplayName());
        assertEquals("Account written off", DefendantAccountStatus.ACCOUNT_WRITTEN_OFF.getDisplayName());
    }

    @Test
    void getLabel_mapsAllSupportedStatuses() {
        assertEquals("L", DefendantAccountStatus.LIVE.getLabel());
        assertEquals("TO", DefendantAccountStatus.TRANSFER_OUT_PENDING.getLabel());
        assertEquals("TS", DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND.getLabel());
        assertEquals("TA", DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED.getLabel());
        assertEquals("CS", DefendantAccountStatus.ACCOUNT_CONSOLIDATED.getLabel());
        assertEquals("WO", DefendantAccountStatus.ACCOUNT_WRITTEN_OFF.getLabel());
    }
}
