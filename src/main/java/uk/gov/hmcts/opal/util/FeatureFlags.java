package uk.gov.hmcts.opal.util;

public final class FeatureFlags {

    public static final String DEFAULT_VALUE_PROPERTY_PREFIX = "launchdarkly.default-flag-values.";
    public static final String RELEASE_1A = "release-1a";
    public static final String RELEASE_1A_ENABLED_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1A;
    public static final String RELEASE_1B = "release-1b";
    public static final String RELEASE_1B_ENABLED_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1B;
    public static final String RELEASE_1C = "release-1c";
    public static final String RELEASE_1C_ENABLED_PROPERTY = DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1C;
    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING =
        RELEASE_1C + "-enforcement-operational-reporting";
    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY =
        DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING;
    public static final String RELEASE_1C_AUTO_ENFORCEMENT_CONFIG =
        RELEASE_1C + "-auto-enforcement-config";
    public static final String RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY =
        DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1C_AUTO_ENFORCEMENT_CONFIG;


    private FeatureFlags() {
    }

    public static String defaultValueProperty(String featureFlag) {
        return DEFAULT_VALUE_PROPERTY_PREFIX + featureFlag;
    }

}
