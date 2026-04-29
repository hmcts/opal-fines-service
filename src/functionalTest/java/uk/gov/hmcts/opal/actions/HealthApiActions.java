package uk.gov.hmcts.opal.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.util.Map;

/**
 * Provides reusable API calls for the service health endpoint.
 */
public class HealthApiActions extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(HealthApiActions.class);

    /**
     * Calls the service health endpoint.
     *
     * @return response returned by the health endpoint.
     */
    public TestHttpResponse getHealth() {
        log.info("Checking fines API health endpoint at {}", getTestUrl());
        return TestHttpClient.get(getTestUrl() + "/health", Map.of());
    }
}
