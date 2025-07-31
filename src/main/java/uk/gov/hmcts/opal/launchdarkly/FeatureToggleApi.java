package uk.gov.hmcts.opal.launchdarkly;

import com.launchdarkly.sdk.ContextBuilder;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LaunchDarklyProperties;

import java.io.IOException;

@Slf4j
@Service
public class FeatureToggleApi {

    private final LDClientInterface internalClient;
    private final LaunchDarklyProperties properties;

    @Autowired
    public FeatureToggleApi(LDClientInterface internalClient,
                            LaunchDarklyProperties properties) {
        this.internalClient = internalClient;
        this.properties = properties;
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLDContext().build(), false);
    }

    public boolean isFeatureEnabled(String feature, boolean defaultValue) {
        return internalClient.boolVariation(feature, createLDContext().build(), defaultValue);
    }

    public boolean isFeatureEnabled(String feature, LDContext context) {
        return internalClient.boolVariation(feature, context, false);
    }


    public boolean isFeatureEnabled(String feature, LDContext context, boolean defaultValue) {
        return internalClient.boolVariation(feature, context, defaultValue);
    }

    public String getFeatureValue(String feature, String defaultValue) {
        String result;
        if (!properties.isEnabled()) {
            result = defaultValue;
        }else {
            result = internalClient.stringVariation(feature, createLDContext().build(), defaultValue);
        }
        log.debug("Feature toggle '{}' value: {}", feature, result);
        return result;
    }

    public ContextBuilder createLDContext() {
        return LDContext.builder(this.properties.getSdkKey())
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", properties.getEnv());
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.error("Error in closing the Launchdarkly client::", e);
        }
    }
}
