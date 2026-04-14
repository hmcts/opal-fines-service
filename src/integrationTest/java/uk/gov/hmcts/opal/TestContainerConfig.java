package uk.gov.hmcts.opal;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Slf4j
public class TestContainerConfig {

    private static final int LEGACY_STUB_PORT = 4553;
    private static final String LOCAL_LEGACY_GATEWAY_URL = "http://localhost:%d/opal".formatted(LEGACY_STUB_PORT);
    private static final String DEFAULT_LEGACY_STUB_IMAGE = "hmctsprod.azurecr.io/opal/legacy-db-stub:latest";
    private static final String LEGACY_STUB_IMAGE =
        System.getenv().getOrDefault("OPAL_LEGACY_STUB_IMAGE", DEFAULT_LEGACY_STUB_IMAGE);
    public static final PostgreSQLContainer POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer(DockerImageName.parse("postgres:17.5"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=200 -c log_connections=on -c log_disconnections=on");

        // Uncomment the following to enable connection to the Test Containers DB whilst debugging.
        //POSTGRES_CONTAINER.setPortBindings(List.of("55432:5432"));

        POSTGRES_CONTAINER.start();

        //Check if the port is available before starting the legacy stub container.
        //This allows a local version of the legacy stub to be used for testing.
        if (isPortAvailable(LEGACY_STUB_PORT)) {
            final GenericContainer<?> legacyStubContainer =
                new GenericContainer<>(DockerImageName.parse(LEGACY_STUB_IMAGE))
                    .withExposedPorts(LEGACY_STUB_PORT);
            legacyStubContainer.setPortBindings(List.of("%d:%d".formatted(LEGACY_STUB_PORT, LEGACY_STUB_PORT)));
            legacyStubContainer.start();
        } else {
            log.warn("Port {} is already in use; reusing the existing legacy gateway at {}.", LEGACY_STUB_PORT,
                legacyGatewayUrl());
        }
    }

    public static String legacyGatewayUrl() {
        return LOCAL_LEGACY_GATEWAY_URL;
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;  // we successfully bound it
        } catch (IOException e) {
            return false; // something else is using it
        }
    }
}
