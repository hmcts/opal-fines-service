package uk.gov.hmcts.opal;

import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;
import static uk.gov.hmcts.opal.TestContainerConfig.REDIS_CONTAINER;

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
import uk.hmcts.zephyr.automation.junit5.extension.ZephyrAutomationExtension;

@SpringBootTest
@ActiveProfiles("integration")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc
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
    }

    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.data.redis.url", REDIS_CONTAINER::getRedisURI);
        registry.add("legacy-gateway.url", TestContainerConfig::legacyGatewayUrl);
    }
}
