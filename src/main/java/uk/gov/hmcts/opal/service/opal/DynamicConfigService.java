package uk.gov.hmcts.opal.service.opal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.AppMode;

@Service
public class DynamicConfigService {

    private static final String APP_MODE_LAUNCH_DARKLY_KEY = "app-mode";
    private final FeatureToggleApi featureToggleApi;
    private final String defaultAppMode;

    public DynamicConfigService(FeatureToggleApi featureToggleApi, @Value("${app-mode}") String defaultMode) {
        this.defaultAppMode = defaultMode;
        this.featureToggleApi = featureToggleApi;
    }

    public AppMode getAppMode() {
        return AppMode.builder()
            .mode(featureToggleApi.getFeatureValue(APP_MODE_LAUNCH_DARKLY_KEY, defaultAppMode))
            .build();
    }
}
