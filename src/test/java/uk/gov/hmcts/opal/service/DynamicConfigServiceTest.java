package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class DynamicConfigServiceTest {

    @Test
    void shouldReturnOpal_whenInvokedWithDefaultValue() {
        String defaultMode = "legacy";
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        doReturn(defaultMode).when(featureToggleService).getFeatureValue("app-mode", defaultMode);
        DynamicConfigService configService = new DynamicConfigService(featureToggleService, defaultMode);
        AppMode mode = configService.getAppMode();
        assertEquals(defaultMode, mode.getMode());
    }
}
