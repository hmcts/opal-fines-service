package uk.gov.hmcts.opal.util;

public final class FeatureFlags {

    public static final String DEFAULT_VALUE_PROPERTY_PREFIX = "launchdarkly.default-flag-values.";
    public static final String RELEASE_1B = "release-1b";
    public static final String RELEASE_1B_DEFAULT_VALUE_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1B;
    public static final String RELEASE_1C = "release-1c";
    public static final String RELEASE_1C_DEFAULT_VALUE_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1C;

    private FeatureFlags() {
    }

    public static String defaultValueProperty(String featureFlag) {
        return DEFAULT_VALUE_PROPERTY_PREFIX + featureFlag;
    }
}
