package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class DestinationTypeTest {

    @Test
    void getDescription_shouldReturnConfiguredDescription() {
        assertEquals("Court Fee", DestinationType.C.getDescription());
        assertEquals("Fines", DestinationType.F.getDescription());
        assertEquals("Suspense", DestinationType.S.getDescription());
    }
}
