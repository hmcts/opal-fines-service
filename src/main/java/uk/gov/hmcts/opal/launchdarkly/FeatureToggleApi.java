package uk.gov.hmcts.opal.launchdarkly;

import com.launchdarkly.sdk.ContextBuilder;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FeatureToggleApi {

    private final LDClientInterface internalClient;
    private final String environment;

    @Autowired
    public FeatureToggleApi(LDClientInterface internalClient, @Value("${launchdarkly.env}") String environment) {
        this.internalClient = internalClient;
        this.environment = environment;
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLDContext().build(), false);
    }

    public boolean isFeatureEnabled(String feature, boolean defaultValue) {
        return internalClient.boolVariation(feature, createLDContext().build(), defaultValue);
    }

    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, LDContext.fromUser(user), false);
    }


    public boolean isFeatureEnabled(String feature, LDUser user, boolean defaultValue) {
        return internalClient.boolVariation(feature, LDContext.fromUser(user), defaultValue);
    }

    public String getFeatureValue(String feature, String defaultValue) {
        return internalClient.stringVariation(feature, createLDContext().build(), defaultValue);
    }

    public ContextBuilder createLDContext() {
        return LDContext.builder("opal")
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", environment);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.error("Error in closing the Launchdarkly client::", e);
        }
    }
}
