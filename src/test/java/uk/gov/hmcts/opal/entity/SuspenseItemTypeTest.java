package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SuspenseItemTypeTest {

    @Test
    void given_enumValues_when_getDescription_then_expectedDescriptionsReturned() {
        assertEquals("Cancelled cheque (Fines)", SuspenseItemType.CC.getDescription());
        assertEquals("Court Fee", SuspenseItemType.CF.getDescription());
        assertEquals("Fines payment in advance", SuspenseItemType.FA.getDescription());
        assertEquals("Cancelled BACS (Fines)", SuspenseItemType.FB.getDescription());
        assertEquals("Adjustment", SuspenseItemType.IN.getDescription());
        assertEquals("Legal aid payment", SuspenseItemType.LA.getDescription());
        assertEquals("Maintenance payment", SuspenseItemType.MA.getDescription());
        assertEquals("Cancelled BACS (Maintenance)", SuspenseItemType.MB.getDescription());
        assertEquals("Cancelled cheque (Maintenance)", SuspenseItemType.MC.getDescription());
        assertEquals("Miscellaneous", SuspenseItemType.MS.getDescription());
        assertEquals("Overpayment (Maintenance)", SuspenseItemType.OM.getDescription());
        assertEquals("Overpayment (Court Fees)", SuspenseItemType.OC.getDescription());
        assertEquals("Overpayment (Fines)", SuspenseItemType.OF.getDescription());
        assertEquals("Unidentified", SuspenseItemType.UN.getDescription());
    }
}
