package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class DynamicConfigServiceTest {

    @Test
    void shouldReturnOpal_whenInvokedWithDefaultValue() {
        String defaultMode = "legacy";
        FeatureToggleApi featureToggleApi = mock(FeatureToggleApi.class);
        doReturn(defaultMode).when(featureToggleApi).getFeatureValue("app-mode", defaultMode);
        DynamicConfigService configService = new DynamicConfigService(featureToggleApi, defaultMode);
        AppMode mode = configService.getAppMode();
        assertEquals(defaultMode, mode.getMode());
    }
}
