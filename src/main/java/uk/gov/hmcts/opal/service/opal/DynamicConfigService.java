package uk.gov.hmcts.opal.service.opal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;

@Service
public class DynamicConfigService {

    private static final String APP_MODE_LAUNCH_DARKLY_KEY = "app-mode";
    private final FeatureToggleService featureToggleService;
    private final String defaultAppMode;

    public DynamicConfigService(FeatureToggleService featureToggleService, @Value("${app-mode}") String defaultMode) {
        this.defaultAppMode = defaultMode;
        this.featureToggleService = featureToggleService;
    }

    public AppMode getAppMode() {
        return AppMode.builder()
            .mode(featureToggleService.getFeatureValue(APP_MODE_LAUNCH_DARKLY_KEY, defaultAppMode))
            .build();
    }
}
