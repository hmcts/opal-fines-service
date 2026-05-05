package uk.gov.hmcts.opal.common.launchdarkly;

/**
 * Centralised catalogue of LaunchDarkly feature flag keys and their corresponding Spring property fallback names for
 * local/offline resolution.
 *
 * <p>This class is intentionally kept as a plain constants class so it can be
 * moved to {@code opal-common-lib} in a future release with no structural changes.
 */
public final class FeatureFlags {

    public static final String RELEASE_1A = "release-1a";
    public static final String RELEASE_1A_ENABLED_PROPERTY = "${launchdarkly.feature.release-1a.enabled:false}";

    public static final String RELEASE_1B = "release-1b";
    public static final String RELEASE_1B_ENABLED_PROPERTY = "${launchdarkly.feature.release-1b.enabled:false}";

    public static final String RELEASE_1C_WRITE_OFF = "release-1c-write-off";
    public static final String RELEASE_1C_WRITE_OFF_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-write-off.enabled:false}";

    public static final String RELEASE_1C_AUTO_ENFORCEMENT = "release-1c-auto-enforcement";
    public static final String RELEASE_1C_AUTO_ENFORCEMENT_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-auto-enforcement.enabled:false}";

    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING =
        "release-1c-enforcement-operational-reporting";
    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-enforcement-operational-reporting.enabled:false}";

    public static final String RELEASE_1C_PRINTING = "release-1c-printing";
    public static final String RELEASE_1C_PRINTING_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-printing.enabled:false}";

    public static final String RELEASE_1C_CPP = "release-1c-cpp";
    public static final String RELEASE_1C_CPP_ENABLED_PROPERTY = "${launchdarkly.feature.release-1c-cpp.enabled:false}";

    public static final String RELEASE_1C_CPP_ENFORCEMENT = "release-1c-cpp-enforcement";
    public static final String RELEASE_1C_CPP_ENFORCEMENT_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-cpp-enforcement.enabled:false}";

    public static final String RELEASE_1C_BANKING_INTERFACES = "release-1c-banking-interfaces";
    public static final String RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-banking-interfaces.enabled:false}";

    public static final String RELEASE_1C_REFERENCE_DATA = "release-1c-reference-data";
    public static final String RELEASE_1C_REFERENCE_DATA_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-reference-data.enabled:false}";

    public static final String RELEASE_1C_R1A_TIDY_UP = "release-1c-r1a-tidy-up";
    public static final String RELEASE_1C_R1A_TIDY_UP_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-r1a-tidy-up.enabled:false}";

    public static final String RELEASE_1C_PAYMENT = "release-1c-payment";
    public static final String RELEASE_1C_PAYMENT_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-payment.enabled:false}";

    public static final String RELEASE_1C_SUSPENSE = "release-1c-suspense";
    public static final String RELEASE_1C_SUSPENSE_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-suspense.enabled:false}";

    public static final String RELEASE_1C_CHECKS = "release-1c-checks";
    public static final String RELEASE_1C_CHECKS_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-checks.enabled:false}";

    public static final String RELEASE_1C_FINANCIAL_MOVEMENTS = "release-1c-financial-movements";
    public static final String RELEASE_1C_FINANCIAL_MOVEMENTS_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1c-financial-movements.enabled:false}";

    public static final String RELEASE_1D_AUTO_ENFORCEMENT_CONFIG = "release-1d-auto-enforcement-config";
    public static final String RELEASE_1D_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1d-auto-enforcement-config.enabled:false}";

    public static final String RELEASE_1D_DATA_RETENTION = "release-1d-data-retention";
    public static final String RELEASE_1D_DATA_RETENTION_ENABLED_PROPERTY =
        "${launchdarkly.feature.release-1d-data-retention.enabled:false}";

    private FeatureFlags() {
        // constants class — no instantiation
    }
}
