package uk.gov.hmcts.opal.assertions;

import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Provides reusable assertions for the health API response.
 */
public class HealthApiAssertions {

    /**
     * Asserts that the health endpoint returned HTTP 200 and reported an UP status.
     *
     * @param response health-endpoint response returned by the test client.
     */
    public void assertServiceIsUp(TestHttpResponse response) {
        assertEquals(200, response.statusCode(), "Unexpected HTTP status");
        assertEquals("UP", response.jsonPath("status"), "Unexpected health status");
    }
}
