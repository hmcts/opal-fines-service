package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PriorityTest {

    @Test
    void getByLabel_shouldReturnEnum_forKnownLabel() {
        assertEquals(Priority.ZERO, Priority.getByLabel("0"));
        assertEquals(Priority.ONE, Priority.getByLabel("1"));
        assertEquals(Priority.TWO, Priority.getByLabel("2"));
    }

    @Test
    void getByLabel_shouldThrow_forUnknownLabel() {
        assertThrows(IllegalArgumentException.class, () -> Priority.getByLabel("unknown"));
    }
}
