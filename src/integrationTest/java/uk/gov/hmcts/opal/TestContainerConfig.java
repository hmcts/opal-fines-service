package uk.gov.hmcts.opal;

import com.redis.testcontainers.RedisContainer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(TestContainerConfig.class);

    private static final int LEGACY_STUB_PORT = 4553;
    private static final String LOCAL_LEGACY_GATEWAY_URL = "http://localhost:%d/opal".formatted(LEGACY_STUB_PORT);
    private static final String DEFAULT_LEGACY_STUB_IMAGE = "hmctsprod.azurecr.io/opal/legacy-db-stub:latest";
    private static final String DEFAULT_POSTGRES_IMAGE = "postgres:17.5";
    private static final String AZURITE_IMAGE = "mcr.microsoft.com/azure-storage/azurite:3.35.0";
    private static final int AZURITE_BLOB_PORT = 10000;
    private static final String AZURITE_ACCOUNT_NAME = "devstoreaccount1";
    private static final String AZURITE_ACCOUNT_KEY =
        "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    private static final String LEGACY_STUB_IMAGE =
        System.getenv().getOrDefault("OPAL_LEGACY_STUB_IMAGE", DEFAULT_LEGACY_STUB_IMAGE);
    private static final boolean ENABLE_LEGACY_STUB =
        !"false".equalsIgnoreCase(System.getenv().getOrDefault("OPAL_ENABLE_LEGACY_STUB", "true"));
    private static final String POSTGRES_IMAGE =
        System.getenv().getOrDefault("OPAL_POSTGRES_IMAGE", DEFAULT_POSTGRES_IMAGE);
    public static final PostgreSQLContainer POSTGRES_CONTAINER;
    public static final RedisContainer REDIS_CONTAINER;
    public static final GenericContainer<?> AZURITE_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("postgres -c max_connections=200 -c log_connections=on -c log_disconnections=on");

        // Uncomment the following to enable connection to the Test Containers DB whilst debugging.
        //POSTGRES_CONTAINER.setPortBindings(List.of("55432:5432"));

        POSTGRES_CONTAINER.start();

        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:6.2.6"))
            .withExposedPorts(6379);
        REDIS_CONTAINER.start();

        AZURITE_CONTAINER = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE))
            .withCommand(
                "azurite-blob --blobHost 0.0.0.0 --blobPort " + AZURITE_BLOB_PORT + " --skipApiVersionCheck")
            .withExposedPorts(AZURITE_BLOB_PORT);
        AZURITE_CONTAINER.start();

        //Check if the port is available before starting the legacy stub container.
        //This allows a local version of the legacy stub to be used for testing.
        if (ENABLE_LEGACY_STUB && isPortAvailable(LEGACY_STUB_PORT)) {
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

    public static String azuriteConnectionString() {
        return "DefaultEndpointsProtocol=http;"
            + "AccountName=" + AZURITE_ACCOUNT_NAME + ";"
            + "AccountKey=" + AZURITE_ACCOUNT_KEY + ";"
            + "BlobEndpoint=http://127.0.0.1:" + AZURITE_CONTAINER.getMappedPort(AZURITE_BLOB_PORT)
            + "/" + AZURITE_ACCOUNT_NAME + ";";
    }
}
