package uk.gov.hmcts.opal.service.hmrc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.config.cache.CacheNames;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Slf4j(topic = "opal.HmrcAuthServiceIntegrationTest")
@DisplayName("HMRC Auth Service Integration Test")
@WireMockTest(httpPort = 8080)
public class HmrcAuthServiceIntegrationTest
    extends AbstractIntegrationTest { // TODO do i want to be inheriting from this?

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HmrcAuthService hmrcAuthService;

    private HMRCAuthToken hmrcAuthToken = new HMRCAuthToken(
        "xxxx-test-token-xxxx", "bearer", 14400, "test-scope1+test-scope2"
    );

    @Override
    @BeforeEach
    public void beforeEach() {
        cacheManager.getCache(CacheNames.HMRC_AUTH_SERVICE).clear();

        stubFor(get(urlPathEqualTo("/oauth/token"))
            .withQueryParam("client_id", equalTo("test-hmrc-client-id"))
            .withQueryParam("client_secret", equalTo("test-hmrc-client-secret"))
            .withQueryParam("grant_type", equalTo("client_credentials"))
            .withQueryParam("scope", equalTo("test-scope1 test-scope2"))
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(hmrcAuthToken))));
    }

    @Test
    void correctlyCallsHmrcEndpointAndReturnsDto() {

        HMRCAuthToken returnedAuthToken = hmrcAuthService.getAuthToken();

        assertAll(
            () -> assertEquals(hmrcAuthToken.getAccessToken(), returnedAuthToken.getAccessToken()),
            () -> assertEquals(hmrcAuthToken.getTokenType(), returnedAuthToken.getTokenType()),
            () -> assertEquals(hmrcAuthToken.getExpiresIn(), returnedAuthToken.getExpiresIn()),
            () -> assertEquals(hmrcAuthToken.getScope(), returnedAuthToken.getScope())
        );
        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/oauth/token")));
    }


    @Test
    void multipleCallsToHmrcEndpoint_UtilisesCache() {
        HMRCAuthToken token1 = hmrcAuthService.getAuthToken();
        HMRCAuthToken token2 = hmrcAuthService.getAuthToken();
        HMRCAuthToken token3 = hmrcAuthService.getAuthToken();

        WireMock.verify(1, getRequestedFor(urlPathEqualTo("/oauth/token")));
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
}
