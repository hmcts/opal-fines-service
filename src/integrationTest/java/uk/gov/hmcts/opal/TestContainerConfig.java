package uk.gov.hmcts.opal;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
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

        // Uncomment the following to enable connection to the Test Containers DB whilst debugging.
        //POSTGRES_CONTAINER.setPortBindings(List.of("55432:5432"));

        POSTGRES_CONTAINER.start();

        if (isPortAvailable(4553)) {
            final GenericContainer<?> LEGACY_STUB_CONTAINER =
                new GenericContainer<>(DockerImageName.parse("sdshmctspublic.azurecr.io/opal/legacy-db-stub:latest"))
                    .withExposedPorts(4553);
            LEGACY_STUB_CONTAINER.setPortBindings(List.of("4553:4553"));
            LEGACY_STUB_CONTAINER.start();
        }
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
