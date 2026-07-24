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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.config.cache.CacheKeys;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

//@ActiveProfiles({"hmrc"})
@Slf4j(topic = "opal.HmrcAuthServiceIntegrationTest")
@DisplayName("HMRC Auth Service Integration Test")
public class HmrcAuthServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestClient restClient;

    private HmrcAuthService hmrcAuthService;

    private HMRCAuthToken hmrcAuthToken = new HMRCAuthToken(
        "xxxx-test-token-xxxx", "bearer", 14400, "test-scope1+test-scope2"
    );

    @BeforeEach
    void setup() {
        hmrcAuthService = new HmrcAuthService( //TODO why is @Value not working
            restClient, "test-hmrc-client-id",
            "test-hmrc-client-secret",
            "test-scope1+test-scope2",
            "http://localhost:4553/oauth/token"
        );
        redisTemplate.delete(CacheKeys.HMRC_AUTH_TOKEN);
    }

    @Test
    void correctlyCallsHmrcEndpointAndReturnsDto() {
        WireMock.configureFor("localhost", 4553);
        stubFor(get("/oauth/token")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(hmrcAuthToken))));

        HMRCAuthToken returnedAuthToken = hmrcAuthService.getAuthToken();

        assertEquals(hmrcAuthToken, returnedAuthToken);
        WireMock.verify(1, getRequestedFor(urlEqualTo("/oauth/token")));
    }

}
