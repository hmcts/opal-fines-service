package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.reference.MappingItem;
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
        List<MappingItem> mappings = List.of(new MappingItem("L", "Live"));
        when(mappingsService.getMappings("defendant-account-status")).thenReturn(mappings);

        ResponseEntity<List<MappingItem>> response = mappingsController.getMappings("defendant-account-status");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mappings, response.getBody());
        verify(mappingsService).getMappings("defendant-account-status");
    }

    @Test
    void getMappings_hasRelease1bFeatureToggleAndPathMapping() throws NoSuchMethodException {
        Method method = MappingsController.class.getMethod("getMappings", String.class);

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);
        assertNotNull(featureToggle);
        assertEquals(FeatureFlags.RELEASE_1B, featureToggle.feature());
        assertEquals(FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty());

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        assertNotNull(getMapping);
        assertArrayEquals(new String[]{"/{type}"}, getMapping.value());
    }
}
