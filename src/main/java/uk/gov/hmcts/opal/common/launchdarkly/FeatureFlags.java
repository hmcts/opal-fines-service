package uk.gov.hmcts.opal.common.launchdarkly;


public final class FeatureFlags {

    public static final String DEFAULT_VALUE_PROPERTY_PREFIX = "launchdarkly.default-flag-values.";
    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING =
        "release-1c-enforcement-operational-reporting";
    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_DEFAULT_VALUE_PROPERTY =
        DEFAULT_VALUE_PROPERTY_PREFIX + RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING;

    private FeatureFlags() {
    }

    public static String defaultValueProperty(String featureFlag) {
        return DEFAULT_VALUE_PROPERTY_PREFIX + featureFlag;
    }
}
