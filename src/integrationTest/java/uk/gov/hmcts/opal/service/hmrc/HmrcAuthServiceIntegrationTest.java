package uk.gov.hmcts.opal.service.hmrc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Slf4j(topic = "opal.HmrcAuthServiceIntegrationTest")
@DisplayName("HMRC Auth Service Integration Test")
public class HmrcAuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HmrcAuthService hmrcAuthService;

    private HMRCAuthToken hmrcAuthToken = new HMRCAuthToken(
        "xxxx-test-token-xxxx", "bearer", 14400, "test-scope1+test-scope2"
    );

    @BeforeEach
    void setupWireMock() {
        WireMock.configureFor("localhost", 4400);
        stubFor(get("/oauth/token")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(hmrcAuthToken))));

        cacheManager.resetCaches();
    }

    @Test
    void correctlyCallsHmrcEndpointAndReturnsDto() {
        HMRCAuthToken returnedAuthToken = hmrcAuthService.getAuthToken();

        assertEquals(hmrcAuthToken, returnedAuthToken);
        WireMock.verify(1, getRequestedFor(urlEqualTo("/oauth/token")));
    }

}
