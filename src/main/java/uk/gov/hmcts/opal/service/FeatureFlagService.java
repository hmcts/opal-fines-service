package uk.gov.hmcts.opal.service;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("launchdarkly")
public class FeatureFlagService {
    private Map<String, String> defaultFlagValues;

    public boolean isFlagEnabled(String flagName) {
        return defaultFlagValues.containsKey(flagName) && defaultFlagValues.get(flagName).equals("true");
    }
}
