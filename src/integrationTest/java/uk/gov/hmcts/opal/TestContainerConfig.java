package uk.gov.hmcts.opal;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    public static final GenericContainer<?> LEGACY_STUB_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.5"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=150 -c log_connections=on -c log_disconnections=on");

        POSTGRES_CONTAINER.start();

        LEGACY_STUB_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("sdshmctspublic.azurecr.io/opal/legacy-db-stub:latest"));

        LEGACY_STUB_CONTAINER.setPortBindings(List.of(
            "4553:4553"
        ));
        LEGACY_STUB_CONTAINER.start();
        LEGACY_STUB_CONTAINER.waitingFor(Wait.forHttp("/"));
    }
}
