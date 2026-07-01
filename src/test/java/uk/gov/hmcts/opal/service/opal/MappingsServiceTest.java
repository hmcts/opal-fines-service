package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;

class MappingsServiceTest {

    private final MappingsService mappingsService = new MappingsService();

    @Test
    void getMappings_returnsSupportedDefendantAccountStatusMappings() {
        List<MappingItemMappings> mappings = mappingsService.getMappings("defendant-account-status");

        assertEquals(List.of(
            MappingItemMappings.builder().code("CS").displayName("Account consolidated").build(),
            MappingItemMappings.builder().code("L").displayName("Live").build(),
            MappingItemMappings.builder().code("TA").displayName("TFO acknowledged").build(),
            MappingItemMappings.builder().code("TO").displayName("TFO to be acknowledged").build(),
            MappingItemMappings.builder().code("TS").displayName("TFO to NI/Scotland to be acknowledged").build(),
            MappingItemMappings.builder().code("WO").displayName("Account written off").build()
        ), mappings);
    }

    @Test
    void getMappings_throwsWhenTypeIsNotAllowListed() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals("Unsupported mapping type: unsupported-type", exception.getMessage());
    }
}
