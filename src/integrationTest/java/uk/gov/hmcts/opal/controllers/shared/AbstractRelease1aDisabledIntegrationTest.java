package uk.gov.hmcts.opal.controllers.shared;

import org.springframework.test.context.TestPropertySource;

/**
 * Shared Spring test context configuration for integration tests that require Release 1A to be disabled.
 */
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1a=false")
public abstract class AbstractRelease1aDisabledIntegrationTest extends AbstractFeatureToggleIntegrationTest {
}
