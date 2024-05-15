package uk.gov.hmcts.opal.launchdarkly;


public class FeatureDisabledException extends RuntimeException {

    public FeatureDisabledException(String message) {
        super(message);
    }
}
