package uk.gov.hmcts.opal;

import org.testcontainers.containers.PostgreSQLContainer;

public class SingletonPostgreSQLContainer {

    private static final PostgreSQLContainer<?> INSTANCE =
        new PostgreSQLContainer<>("postgres:17.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        INSTANCE.start(); // Start the container when the class is loaded
    }

    public static PostgreSQLContainer<?> getInstance() {
        return INSTANCE;
    }
}
