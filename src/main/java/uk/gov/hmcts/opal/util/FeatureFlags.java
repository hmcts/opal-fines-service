package uk.gov.hmcts.opal.util;

public final class FeatureFlags {

    public static final String DEFAULT_VALUE_PROPERTY_PREFIX = "launchdarkly.default-flag-values.";
    public static final String RELEASE_1A = "release-1a";
    public static final String RELEASE_1A_ENABLED_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1A;
    public static final String RELEASE_1B = "release-1b";
    public static final String RELEASE_1B_DEFAULT_VALUE_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1B;

    private FeatureFlags() {
    }

    public static String defaultValueProperty(String featureFlag) {
        return DEFAULT_VALUE_PROPERTY_PREFIX + featureFlag;
    }

}
