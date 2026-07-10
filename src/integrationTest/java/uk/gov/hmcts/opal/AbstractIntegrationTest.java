package uk.gov.hmcts.opal;

import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;
import static uk.gov.hmcts.opal.TestContainerConfig.REDIS_CONTAINER;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.support.UserStateStub;
import uk.hmcts.zephyr.automation.junit5.extension.ZephyrAutomationExtension;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc(htmlUnit = @AutoConfigureMockMvc.HtmlUnit(webClient = false, webDriver = false))
@Import(IntegrationSecurityConfiguration.class)
@ExtendWith(ZephyrAutomationExtension.class)
@SuppressWarnings({"java:S6813", "SpringJavaInjectionPointsAutowiringInspection"})
public abstract class AbstractIntegrationTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Limit JdbcTemplate use to narrow test setup or persistence-side-effect checks.
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private CacheManager cacheManager;

    protected UserStateStub userStateStub;

    public void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @BeforeEach
    public void beforeEach() {
        clearCaches();
        resetWireMock();
        userStateStub = createUserStateStub();
    }

    private void resetWireMock() {
        URI legacyGatewayUri = URI.create(TestContainerConfig.legacyGatewayUrl());
        WireMock.configureFor(legacyGatewayUri.getHost(), legacyGatewayUri.getPort());
        try {
            WireMock.reset();
        } catch (RuntimeException ex) {
            log.debug("Skipping WireMock reset because legacy gateway is unavailable at {}",
                TestContainerConfig.legacyGatewayUrl(), ex);
        }
    }

    protected UserStateStub createUserStateStub() {
        return new UserStateStub();
    }

    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.data.redis.url", REDIS_CONTAINER::getRedisURI);
        registry.add("legacy-gateway.url", TestContainerConfig::legacyGatewayUrl);
        registry.add("opal.report.storage.connection-string", TestContainerConfig::azuriteConnectionString);
    }
}
