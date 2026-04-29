package uk.gov.hmcts.opal.service.opal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleService;

@Service
@Slf4j(topic = "opal.DynamicConfigService")
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

    public boolean isFeatureEnabled(String featureKey) {
        return featureToggleService.isFeatureEnabled(featureKey);
    }

    public void verifyFeatureEnabled(String featureKey, String operationName) {
        boolean enabled = isFeatureEnabled(featureKey);
        log.debug(":verifyFeatureEnabled: feature={} enabled={} operation={}", featureKey, enabled, operationName);
        if (!enabled) {
            log.debug(":verifyFeatureEnabled: blocked operation={} feature={}", operationName, featureKey);
            throw new FeatureDisabledException(operationName + " is disabled by feature flag " + featureKey);
        }
    }
}
