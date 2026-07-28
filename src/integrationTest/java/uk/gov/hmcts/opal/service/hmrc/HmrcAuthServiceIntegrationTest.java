package uk.gov.hmcts.opal.service.hmrc;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.config.cache.CacheNames;
import uk.gov.hmcts.opal.service.hmrc.response.HmrcAuthToken;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Slf4j(topic = "opal.HmrcAuthServiceIntegrationTest")
@DisplayName("HMRC Auth Service Integration Test")
@WireMockTest(httpPort = 8080)
public class HmrcAuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HmrcAuthService hmrcAuthService;

    @Autowired
    private HmrcAuthCreds hmrcAuthCreds;

    private final HmrcAuthToken hmrcAuthToken = new HmrcAuthToken(
        "xxxx-test-token-xxxx", "bearer", 14400, "test-scope1+test-scope2"
    );

    private final HmrcAuthToken hmrcAuthToken2 = new HmrcAuthToken(
        "xxxx-2-test-token-2-xxxx", "bearer", 14400, "test-scope1+test-scope2"
    );

    private static final String WIREMOCK_HMRC_AUTH_SCENARIO = "GET HMRC auth token test";
    private static final String WIREMOCK_STATE__ONE_CALL_MADE = "One call made";
    private static final String WIREMOCK_STATE__SERVER_ERROR = "Server error";
    private static final String WIREMOCK_STATE__CLIENT_ERROR = "Client error";

    private boolean clearAuthServiceCache() {
        return cacheManager.getCache(CacheNames.HMRC_AUTH_SERVICE).invalidate();
    }

    @Override
    @BeforeEach
    public void beforeEach() {
        clearAuthServiceCache();

        String payloadJson = objectMapper.writeValueAsString(hmrcAuthCreds);

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withRequestBody(equalToJson(payloadJson))
            .inScenario(WIREMOCK_HMRC_AUTH_SCENARIO)
            .whenScenarioStateIs(STARTED)
            .willReturn(okJson(objectMapper.writeValueAsString(hmrcAuthToken)))
            .willSetStateTo(WIREMOCK_STATE__ONE_CALL_MADE));

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withRequestBody(equalToJson(payloadJson))
            .inScenario(WIREMOCK_HMRC_AUTH_SCENARIO)
            .whenScenarioStateIs(WIREMOCK_STATE__ONE_CALL_MADE)
            .willReturn(okJson(objectMapper.writeValueAsString(hmrcAuthToken2))));

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withRequestBody(equalToJson(payloadJson))
            .inScenario(WIREMOCK_HMRC_AUTH_SCENARIO)
            .whenScenarioStateIs(WIREMOCK_STATE__SERVER_ERROR)
            .willReturn(serverError()));

        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withRequestBody(equalToJson(payloadJson))
            .inScenario(WIREMOCK_HMRC_AUTH_SCENARIO)
            .whenScenarioStateIs(WIREMOCK_STATE__SERVER_ERROR)
            .willReturn(serverError()));

        // This is a slight hack, as quite hard to modify payload to am
        // invalid one without rewiring service.
        stubFor(post(urlPathEqualTo("/oauth/token"))
            .withRequestBody(equalToJson(payloadJson))
            .inScenario(WIREMOCK_HMRC_AUTH_SCENARIO)
            .whenScenarioStateIs(WIREMOCK_STATE__CLIENT_ERROR)
            .willReturn(badRequest()));
    }

    @Test
    @DisplayName("Correctly calls HMRC endpoint and returns correct DTO (INT.01 - INT.06 - INT.07)")
    @JiraStory("PO-2383")
    @JiraEpic("PO-1421")
    void correctlyCallsHmrcEndpointAndReturnsDto() {

        HmrcAuthToken returnedAuthToken = hmrcAuthService.getAuthToken();

        assertAll(
            () -> assertEquals(hmrcAuthToken.getAccessToken(), returnedAuthToken.getAccessToken()),
            () -> assertEquals(hmrcAuthToken.getTokenType(), returnedAuthToken.getTokenType()),
            () -> assertEquals(hmrcAuthToken.getExpiresIn(), returnedAuthToken.getExpiresIn()),
            () -> assertEquals(hmrcAuthToken.getScope(), returnedAuthToken.getScope())
        );
        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/oauth/token")));
    }


    @Test
    @DisplayName("Multiple calls reuse cache with no external call to HMRC (INT.03 - INT.10)")
    @JiraStory("PO-2383")
    @JiraEpic("PO-1421")
    void multipleCalls_UtilisesCache_NoExternalCall() {
        final HmrcAuthToken token1 = hmrcAuthService.getAuthToken();
        final HmrcAuthToken token2 = hmrcAuthService.getAuthToken();
        final HmrcAuthToken token3 = hmrcAuthService.getAuthToken();

        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/oauth/token")));
        assertAll(
            () -> assertEquals(token1.getAccessToken(), token2.getAccessToken()),
            () -> assertEquals(token1.getAccessToken(), token3.getAccessToken()),
            () -> assertEquals(token1.getTokenType(), token2.getTokenType()),
            () -> assertEquals(token1.getTokenType(), token3.getTokenType()),
            () -> assertEquals(token1.getExpiresIn(), token2.getExpiresIn()),
            () -> assertEquals(token1.getExpiresIn(), token3.getExpiresIn()),
            () -> assertEquals(token1.getScope(), token2.getScope()),
            () -> assertEquals(token1.getScope(), token3.getScope())
        );
    }

    @Test
    @DisplayName("Correctly calls HMRC endpoint multiple times when cache cleared (INT.02)")
    @JiraStory("PO-2383")
    @JiraEpic("PO-1421")
    void correctlyCallsHmrcEndpointMultipleTimes() throws InterruptedException {

        HmrcAuthToken token1 = hmrcAuthService.getAuthToken();
        Thread.sleep(500); // Sleeping to give time for the cache to exist in Redis
        assertTrue(clearAuthServiceCache());
        HmrcAuthToken token2 = hmrcAuthService.getAuthToken();

        WireMock.verify(2, postRequestedFor(urlPathEqualTo("/oauth/token")));
        assertNotEquals(token1.getAccessToken(), token2.getAccessToken());
    }

    @Test
    @DisplayName("Throws HttpServerErrorException when Server Error reported by HMRC endpoint (INT.08)")
    @JiraStory("PO-2383")
    @JiraEpic("PO-1421")
    void throwsCorrectExceptionOnHmrcServerError() {

        WireMock.setScenarioState(WIREMOCK_HMRC_AUTH_SCENARIO, WIREMOCK_STATE__SERVER_ERROR);
        assertThrows(HttpServerErrorException.class, () -> hmrcAuthService.getAuthToken());
    }

    @Test
    @DisplayName("Throws HttpClientErrorException when Client Error reported by HMRC endpoint (INT.08)")
    @JiraStory("PO-2383")
    @JiraEpic("PO-1421")
    void throwsCorrectExceptionOnHmrcClientError() {
        WireMock.setScenarioState(WIREMOCK_HMRC_AUTH_SCENARIO, WIREMOCK_STATE__CLIENT_ERROR);
        assertThrows(HttpClientErrorException.class, () -> hmrcAuthService.getAuthToken());
    }
}
