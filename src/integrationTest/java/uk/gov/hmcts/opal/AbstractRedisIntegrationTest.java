package uk.gov.hmcts.opal;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration-redis")
public abstract class AbstractRedisIntegrationTest extends AbstractIntegrationTest {
}
