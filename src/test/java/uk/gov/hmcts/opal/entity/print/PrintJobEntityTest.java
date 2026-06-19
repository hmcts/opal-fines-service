package uk.gov.hmcts.opal.entity.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrintJobEntityTest {

    @Test
    void shouldDefaultStatusToPending_whenUsingNoArgsConstructor() {
        PrintJobEntity printJobEntity = new PrintJobEntity();

        assertEquals(PrintStatus.PENDING, printJobEntity.getStatus());
    }

    @Test
    void shouldDefaultStatusToPending_whenUsingBuilder() {
        PrintJobEntity printJobEntity = PrintJobEntity.builder().build();

        assertEquals(PrintStatus.PENDING, printJobEntity.getStatus());
    }
}
