package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.reference.MappingItem;

class MappingsServiceTest {

    private final MappingsService mappingsService = new MappingsService();

    @Test
    void getMappings_returnsSupportedDefendantAccountStatusMappings() {
        List<MappingItem> mappings = mappingsService.getMappings("defendant-account-status");

        assertEquals(List.of(
            new MappingItem("CS", "Account consolidated"),
            new MappingItem("L", "Live"),
            new MappingItem("TA", "TFO acknowledged"),
            new MappingItem("TO", "TFO to be acknowledged"),
            new MappingItem("TS", "TFO to NI/Scotland to be acknowledged"),
            new MappingItem("WO", "Account written off")
        ), mappings);
    }

    @Test
    void getMappings_throwsWhenTypeIsNotAllowListed() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals("Unsupported mapping type: unsupported-type", exception.getMessage());
    }
}
