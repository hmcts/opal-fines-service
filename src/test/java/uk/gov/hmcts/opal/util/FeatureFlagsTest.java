package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FeatureFlagsTest {

    @Test
    void defaultValueProperty_returnsCorrectPropertyKey() {
        assertEquals("launchdarkly.default-flag-values.release-1a", FeatureFlags.defaultValueProperty("release-1a"));
    }

    @Test
    void refDataMessageProcessingEnabledProperty_hasCorrectPropertyKey() {
        assertEquals("launchdarkly.default-flag-values.ref-data-message-processing",
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY);
    }
}
