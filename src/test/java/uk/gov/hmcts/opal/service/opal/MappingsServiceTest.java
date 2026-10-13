package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
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
        UnsupportedMappingTypeException exception = assertThrows(UnsupportedMappingTypeException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals(
            "Unsupported mapping type: unsupported-type. Supported types: defendant-account-status, file-type",
            exception.getMessage()
        );
        assertEquals(List.of("defendant-account-status", "file-type"), exception.getSupportedTypes());
    }

    @Test
    void getMappings_returnsSupportedFileTypeMappings() {
        List<MappingItemMappings> mappings = mappingsService.getMappings("file-type");

        assertEquals(List.of(
            MappingItemMappings.builder().code("CSV").displayName("CSV").mimeType("text/csv").build(),
            MappingItemMappings.builder().code("PDF").displayName("PDF").mimeType("application/pdf").build(),
            MappingItemMappings.builder().code("JSON").displayName("JSON").mimeType("application/json").build(),
            MappingItemMappings.builder().code("XML").displayName("XML").mimeType("application/xml").build()
        ), mappings);
    }
}
