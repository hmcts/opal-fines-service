package uk.gov.hmcts.opal.controllers.shared;

import org.springframework.test.context.TestPropertySource;

/**
 * Shared Spring test context configuration for integration tests that require Release 1B to be disabled.
 */
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1b=false")
public abstract class AbstractRelease1bDisabledIntegrationTest extends AbstractFeatureToggleIntegrationTest {
}
