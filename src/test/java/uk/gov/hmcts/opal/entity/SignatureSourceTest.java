package uk.gov.hmcts.opal.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureSourceTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(SignatureSource.AREA, SignatureSource.getByLabel("Area"));
        assertEquals(SignatureSource.LJA, SignatureSource.getByLabel("LJA"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> SignatureSource.getByLabel("unknown"));
    }
}
