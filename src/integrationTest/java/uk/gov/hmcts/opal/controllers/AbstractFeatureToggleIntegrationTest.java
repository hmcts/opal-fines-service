package uk.gov.hmcts.opal.controllers;

import org.springframework.http.MediaType;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.UserStateService;

/**
 * Base class for feature-toggle integration tests.
 * Provides shared constants and the withAuth() helper.
 * Each release gets its own subclass (Release1AFeatureToggleIntegrationTest, etc.).
 */
abstract class AbstractFeatureToggleIntegrationTest extends AbstractIntegrationTest {

    static final String AUTH_HEADER      = "authorization";
    static final String AUTH_VALUE       = "Bearer test";
    static final String IF_MATCH_HEADER  = "If-Match";
    static final String IF_MATCH_VALUE   = "\"0\"";

    @MockitoBean
    UserStateService userStateService;

    static MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request) {
        return request.header(AUTH_HEADER, AUTH_VALUE).accept(MediaType.APPLICATION_JSON);
    }

    static MockHttpServletRequestBuilder withAuthAndJson(MockHttpServletRequestBuilder request) {
        return withAuth(request).contentType(MediaType.APPLICATION_JSON);
    }

    static MockHttpServletRequestBuilder withAuthJsonAndIfMatch(MockHttpServletRequestBuilder request) {
        return withAuthAndJson(request).header(IF_MATCH_HEADER, IF_MATCH_VALUE);
    }

    static Arguments args(String description, MockHttpServletRequestBuilder request) {
        return Arguments.of(description, request);
    }
}