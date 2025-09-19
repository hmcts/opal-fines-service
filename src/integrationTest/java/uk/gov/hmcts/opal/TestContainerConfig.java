package uk.gov.hmcts.opal;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.5"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=150 -c log_connections=on -c log_disconnections=on");

        POSTGRES_CONTAINER.start();
    }
}
