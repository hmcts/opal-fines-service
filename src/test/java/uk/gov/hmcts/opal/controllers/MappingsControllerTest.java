package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.exception.MissingMappingTypeException;
import uk.gov.hmcts.opal.generated.http.api.MappingsApi;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.opal.MappingsService;
import uk.gov.hmcts.opal.util.FeatureFlags;

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
        when(mappingsService.getSupportedMappingTypes()).thenReturn(List.of("defendant-account-status"));

        MissingMappingTypeException exception = assertThrows(
            MissingMappingTypeException.class,
            () -> mappingsController.getMappingsWithoutType()
        );

        assertEquals(
            "Required mapping type is missing. Supported types: defendant-account-status",
            exception.getMessage()
        );
        assertEquals(List.of("defendant-account-status"), exception.getSupportedTypes());
        verify(mappingsService).getSupportedMappingTypes();
    }

    @Test
    void getMappings_hasRelease1bFeatureToggleAndImplementsGeneratedApi() throws NoSuchMethodException {
        Method method = MappingsController.class.getMethod("getMappings", String.class);

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);
        assertNotNull(featureToggle);
        assertEquals(FeatureFlags.RELEASE_1B, featureToggle.feature());
        assertEquals(FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty());
        assertTrue(MappingsApi.class.isAssignableFrom(MappingsController.class));
    }

    @Test
    void getMappingsWithoutType_hasRelease1bFeatureToggle() throws NoSuchMethodException {
        Method method = MappingsController.class.getMethod("getMappingsWithoutType");

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);
        assertNotNull(featureToggle);
        assertEquals(FeatureFlags.RELEASE_1B, featureToggle.feature());
        assertEquals(FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty());
    }
}
