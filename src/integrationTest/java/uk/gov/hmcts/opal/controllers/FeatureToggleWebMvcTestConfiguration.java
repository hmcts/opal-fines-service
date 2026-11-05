package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.mock;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggleAspect;
import uk.gov.hmcts.opal.common.launchdarkly.config.LaunchDarklyProperties;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.config.ClockConfiguration;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

/**
 * MVC-slice support for endpoints protected by {@link uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle}.
 *
 * <p>The real feature-toggle API evaluates the test's {@code launchdarkly.default-flag-values} properties. The
 * LaunchDarkly client is intentionally inert because disabled local defaults do not call it.</p>
 */
@TestConfiguration(proxyBeanMethods = false)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({
    ClockConfiguration.class,
    FeatureToggleAspect.class,
    FeatureToggleApi.class,
    JsonSchemaValidationService.class,
    LaunchDarklyProperties.class,
    OpalGlobalExceptionHandler.class
})
public class FeatureToggleWebMvcTestConfiguration {

    @Bean
    LDClientInterface ldClient() {
        return mock(LDClientInterface.class);
    }
}
