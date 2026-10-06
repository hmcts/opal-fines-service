package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeatureFlagServiceTest {

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagService();
        featureFlagService.setDefaultFlagValues(Map.of(
            "release-1b","true",
            "release-1b-1-1", "false"));
    }

    @Test
    void isFlagEnabled_whenFlagDoesNotExist_returnsFalse() {
        assertFalse(featureFlagService.isFlagEnabled("not-a-flag"));
    }

    @Test
    void isFlagEnabled_whenFlagIsFalse_returnsFalse() {
        assertFalse(featureFlagService.isFlagEnabled("release-1b-1-1"));
    }

    @Test
    void isFlagEnabled_whenFlagIsTrue_returnsTrue() {
        assertTrue(featureFlagService.isFlagEnabled("release-1b"));
    }
}
