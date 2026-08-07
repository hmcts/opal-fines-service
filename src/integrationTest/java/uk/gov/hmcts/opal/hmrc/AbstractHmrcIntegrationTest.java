package uk.gov.hmcts.opal.hmrc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;
import static uk.gov.hmcts.opal.TestContainerConfig.REDIS_CONTAINER;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.Application;
import uk.gov.hmcts.opal.TestContainerConfig;

@SpringBootTest(classes = Application.class)
@EnableFeignClients
@ActiveProfiles("integration")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfiguration.class})
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@ComponentScan
public class AbstractHmrcIntegrationTest {

    private static final String wiremockHost = "localhost";
    private static final int wiremockPort = 3000;

    private static String getWiremockUrl() {
        return wiremockHost + ":" + wiremockPort;
    }

    protected static final String exampleMatchId = "57072660-1df9-4aeb-b4ea-cd2d7f96e430";
    protected static final String exampleCorrelationId = "57072660-1df9-4aeb-b4ea-cd2d7f96e430";
    protected static final String exampleAcceptHeader = "application/vnd.hmrc.2.0+json";

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static WireMockServer mockServer;

    @BeforeAll
    public static void beforeAll() {
        mockServer = new WireMockServer(wiremockPort);
        mockServer.start();
    }

    @AfterAll
    public static void teardown() {
        mockServer.stop();
    }


    @BeforeEach
    public void beforeEach() {
        resetWireMock();
    }

    private void resetWireMock() {
        WireMock.configureFor(wiremockHost, wiremockPort);
        try {
            WireMock.reset();
        } catch (RuntimeException ex) {
            log.debug("Skipping WireMock reset because legacy gateway is unavailable at {}",
                TestContainerConfig.legacyGatewayUrl(), ex);
        }
    }

    protected void withSuccessfulMock(String url, String body) {
        stubFor(get(urlPathEqualTo(url))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    protected void withUnsuccessfulMock(String url, HttpStatus status, String body) {
        stubFor(get(urlPathEqualTo(url))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    protected void withTimeoutMock(String url, int delay) {
        stubFor(get(urlPathEqualTo(url))
            .willReturn(aResponse()
                .withFixedDelay(delay)));
    }

    protected void verifyRequestHeader(String authHeader) {
        mockServer.verify(
            getRequestedFor(anyUrl())
                .withHeader("Authorization", equalTo(authHeader))
        );
    }

    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.data.redis.url", REDIS_CONTAINER::getRedisURI);
        registry.add("legacy-gateway.url", TestContainerConfig::legacyGatewayUrl);
        registry.add("individuals.url", AbstractHmrcIntegrationTest::getWiremockUrl);
    }
}
