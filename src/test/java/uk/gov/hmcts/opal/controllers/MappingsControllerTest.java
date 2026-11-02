package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.exception.MissingMappingTypeException;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.opal.MappingsService;

@ExtendWith(MockitoExtension.class)
class MappingsControllerTest {

    @Mock
    private MappingsService mappingsService;

    @InjectMocks
    private MappingsController mappingsController;

    @Test
    void getMappings_returnsMappingsForType() {
        List<MappingItemMappings> mappings = List.of(
            MappingItemMappings.builder().code("L").displayName("Live").build()
        );
        when(mappingsService.getMappings("defendant-account-status")).thenReturn(mappings);

        ResponseEntity<List<MappingItemMappings>> response = mappingsController.getMappings("defendant-account-status");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mappings, response.getBody());
        verify(mappingsService).getMappings("defendant-account-status");
    }

    @Test
    void getMappingsWithoutType_throwsMissingMappingTypeException() {
        when(mappingsService.getMappingTypesOrThrowIfNoneEnabled()).thenReturn(List.of("defendant-account-status"));

        MissingMappingTypeException exception = assertThrows(
            MissingMappingTypeException.class,
            () -> mappingsController.getMappingsWithoutType()
        );

        assertEquals(
            "Required mapping type is missing. Supported types: defendant-account-status",
            exception.getMessage()
        );
        assertEquals(List.of("defendant-account-status"), exception.getSupportedTypes());
        verify(mappingsService).getMappingTypesOrThrowIfNoneEnabled();
    }
}
