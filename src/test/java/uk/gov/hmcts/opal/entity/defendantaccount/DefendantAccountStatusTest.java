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
    void getCode_mapsAllSupportedStatuses() {
        assertEquals("L", DefendantAccountStatus.LIVE.getCode());
        assertEquals("TO", DefendantAccountStatus.TRANSFER_OUT_PENDING.getCode());
        assertEquals("TS", DefendantAccountStatus.TRANSFER_OUT_TO_NI_SCOTLAND.getCode());
        assertEquals("TA", DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED.getCode());
        assertEquals("CS", DefendantAccountStatus.ACCOUNT_CONSOLIDATED.getCode());
        assertEquals("WO", DefendantAccountStatus.ACCOUNT_WRITTEN_OFF.getCode());
    }
}
