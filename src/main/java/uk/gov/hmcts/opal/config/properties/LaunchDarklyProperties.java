package uk.gov.hmcts.opal.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "launchdarkly")
public class LaunchDarklyProperties {

    /**
     * true to enable launchdarkly.
     */
    private Boolean enabled;

    /**
     * sdk key to connect to launchdarkly.
     */
    private String sdkKey;

    /**
     * true to use launchdarkly offline mode.
     */
    private Boolean offlineMode;

    /**
     * (optional) a list of paths to json or yaml files containing flags for launchdarkly.
     * If there are duplicate keys, the first files have precedence.
     */
    private String[] file;

    private String env;

    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
