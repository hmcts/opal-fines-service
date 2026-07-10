package uk.gov.hmcts.opal.service.opal;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;

@Service
public class DynamicConfigService {

    private static final String IS_LEGACY_MODE_LAUNCH_DARKLY_KEY = "is-legacy-mode";

    private final FeatureToggleApi featureToggleApi;

    public DynamicConfigService(FeatureToggleApi featureToggleApi) {
        this.featureToggleApi = featureToggleApi;
    }

    public boolean isLegacyMode() {
        return featureToggleApi.isFeatureEnabled(IS_LEGACY_MODE_LAUNCH_DARKLY_KEY);
    }
}
