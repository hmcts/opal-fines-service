package uk.gov.hmcts.opal.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class DynamicConfigServiceTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnIsLegacyModeFlagValue(boolean legacyMode) {
        FeatureToggleApi featureToggleApi = mock(FeatureToggleApi.class);
        doReturn(legacyMode).when(featureToggleApi).isFeatureEnabled("is-legacy-mode");
        DynamicConfigService configService = new DynamicConfigService(featureToggleApi);
        assertEquals(legacyMode, configService.isLegacyMode());
    }
}
