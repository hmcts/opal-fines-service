package uk.gov.hmcts.opal.common.launchdarkly;

/**
 * Centralised catalogue of LaunchDarkly feature flag keys and their corresponding Spring property fallback names for
 * local/offline resolution.
 *
 * <p>This class is intentionally kept as a plain constants class so it can be
 * moved to {@code opal-common-lib} in a future release with no structural changes.
 */
public final class FeatureFlags {

    public static final String RELEASE_1C_ENFORCEMENT_OPERATIONAL_REPORTING =
        "release-1c-enforcement-operational-reporting";


    private FeatureFlags() {
        // constants class — no instantiation
    }
}
