package uk.gov.hmcts.opal;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    private static final int LEGACY_STUB_PORT = 4553;
    private static final String LOCAL_LEGACY_GATEWAY_URL = "http://localhost:%d/opal".formatted(LEGACY_STUB_PORT);
    private static final String EXTERNAL_LEGACY_GATEWAY_URL = System.getenv("OPAL_LEGACY_GATEWAY_URL");
    private static final Path LOCAL_LEGACY_STUB_WIREMOCK_PATH = Path.of("")
        .toAbsolutePath()
        .resolveSibling("opal-legacy-db-stub")
        .resolve("wiremock");

    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    public static final GenericContainer<?> LEGACY_STUB_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.5"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=200 -c log_connections=on -c log_disconnections=on");

        // Uncomment the following to enable connection to the Test Containers DB whilst debugging.
        //POSTGRES_CONTAINER.setPortBindings(List.of("55432:5432"));

        POSTGRES_CONTAINER.start();

        if (shouldStartLegacyStubContainer()) {
            LEGACY_STUB_CONTAINER = createLegacyStubContainer();
            LEGACY_STUB_CONTAINER.start();
        } else {
            LEGACY_STUB_CONTAINER = null;
        }
    }

    public static String legacyGatewayUrl() {
        if (EXTERNAL_LEGACY_GATEWAY_URL != null && !EXTERNAL_LEGACY_GATEWAY_URL.isBlank()) {
            return EXTERNAL_LEGACY_GATEWAY_URL;
        }

        if (!isPortAvailable(LEGACY_STUB_PORT)) {
            return LOCAL_LEGACY_GATEWAY_URL;
        }

        return "http://%s:%d/opal".formatted(
            LEGACY_STUB_CONTAINER.getHost(),
            LEGACY_STUB_CONTAINER.getMappedPort(LEGACY_STUB_PORT)
        );
    }

    private static boolean shouldStartLegacyStubContainer() {
        return (EXTERNAL_LEGACY_GATEWAY_URL == null || EXTERNAL_LEGACY_GATEWAY_URL.isBlank())
            && isPortAvailable(LEGACY_STUB_PORT);
    }

    private static GenericContainer<?> createLegacyStubContainer() {
        GenericContainer<?> legacyStubContainer =
            new GenericContainer<>(DockerImageName.parse("sdshmctspublic.azurecr.io/opal/legacy-db-stub:latest"))
                .withExposedPorts(LEGACY_STUB_PORT);

        localLegacyWiremockPath().ifPresent(path -> {
            legacyStubContainer.withFileSystemBind(
                path.toString(), "/tmp/opal-legacy-db-stub/wiremock", BindMode.READ_ONLY);
            legacyStubContainer.withEnv("WIREMOCK_SERVER_MAPPINGS_PATH", "/tmp/opal-legacy-db-stub/wiremock");
        });

        return legacyStubContainer;
    }

    private static Optional<Path> localLegacyWiremockPath() {
        if (Files.isDirectory(LOCAL_LEGACY_STUB_WIREMOCK_PATH)) {
            return Optional.of(LOCAL_LEGACY_STUB_WIREMOCK_PATH);
        }

        return Optional.empty();
    }

    static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
