package uk.gov.hmcts.opal.util;

public final class FeatureFlags {

    public static final String RELEASE_1B = "release-1b";
    public static final String RELEASE_1B_DEFAULT_VALUE_PROPERTY =
        "launchdarkly.default-flag-values.release-1b";

    private FeatureFlags() {
    }
}
