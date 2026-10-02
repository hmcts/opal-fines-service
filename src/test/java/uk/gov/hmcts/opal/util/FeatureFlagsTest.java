package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    @Test
    void defaultValueProperty_returnsCorrectPropertyKey() {
        assertEquals("launchdarkly.default-flag-values.release-1a", FeatureFlags.defaultValueProperty("release-1a"));
    }

    @Test
    void release1b11Constants_matchLaunchDarklyKeyAndDefaultProperty() {
        assertEquals("release-1b-1-1", FeatureFlags.RELEASE_1B_1_1);
        assertEquals(
            "launchdarkly.default-flag-values.release-1b-1-1",
            FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY
        );
    }
}
