package uk.gov.hmcts.opal.controllers.shared;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

/**
 * Base class for feature-toggle integration tests.
 * Provides shared constants and the withAuth() helper.
 * Each release gets its own subclass (Release1AFeatureToggleIntegrationTest, etc.).
 */
public abstract class AbstractFeatureToggleIntegrationTest extends AbstractIntegrationTest {

    protected static final String AUTH_HEADER      = "authorization";
    protected static final String AUTH_VALUE       = "Bearer test";
    protected static final String IF_MATCH_HEADER  = "If-Match";
    protected static final String IF_MATCH_VALUE   = "\"0\"";


    protected static MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request) {
        return request.header(AUTH_HEADER, AUTH_VALUE).accept(MediaType.APPLICATION_JSON);
    }

    protected static MockHttpServletRequestBuilder withAuthAndJson(MockHttpServletRequestBuilder request) {
        return withAuth(request).contentType(MediaType.APPLICATION_JSON);
    }

    protected static MockHttpServletRequestBuilder withAuthJsonAndIfMatch(MockHttpServletRequestBuilder request) {
        return withAuthAndJson(request).header(IF_MATCH_HEADER, IF_MATCH_VALUE);
    }

    protected static Arguments args(String description, MockHttpServletRequestBuilder request) {
        return Arguments.of(description, request);
    }
}
