package uk.gov.hmcts.opal.config;

import java.util.Optional;

/** Resolves environment-specific settings used by functional-test fixtures. */
public final class TestEnvironment {

    private static final String DEFAULT_DATABASE_NAME = "opal-fines-db";
    private static final String DEFAULT_DATABASE_PORT = "5432";
    private static final String DEFAULT_DATABASE_USERNAME = "opal-db-user";
    private static final String DEFAULT_DATABASE_PASSWORD = "opal-db-password";

    private TestEnvironment() {
    }

    /**
     * Returns the database JDBC URL for functional-test fixtures.
     * FUNCTIONAL_TEST_DB_URL takes precedence over the application's database settings.
     */
    public static String getDatabaseUrl() {
        return get("FUNCTIONAL_TEST_DB_URL")
            .map(TestEnvironment::toPostgresJdbcUrl)
            .orElseGet(TestEnvironment::getApplicationDatabaseUrl);
    }

    public static String getDatabaseUsername() {
        return get("FUNCTIONAL_TEST_DB_USERNAME")
            .or(() -> get("OPAL_FINES_DB_USERNAME"))
            .orElse(DEFAULT_DATABASE_USERNAME);
    }

    public static String getDatabasePassword() {
        return get("FUNCTIONAL_TEST_DB_PASSWORD")
            .or(() -> get("OPAL_FINES_DB_PASSWORD"))
            .orElse(DEFAULT_DATABASE_PASSWORD);
    }

    private static String getApplicationDatabaseUrl() {
        return get("OPAL_FINES_DB_HOST")
            .map(host -> "jdbc:postgresql://" + host
                + ":" + get("OPAL_FINES_DB_PORT").orElse(DEFAULT_DATABASE_PORT)
                + "/" + get("OPAL_FINES_DB_NAME").orElse(DEFAULT_DATABASE_NAME)
                + get("OPAL_FINES_DB_OPTIONS").orElse(""))
            .orElse("jdbc:postgresql://localhost:" + DEFAULT_DATABASE_PORT + "/" + DEFAULT_DATABASE_NAME);
    }

    private static String toPostgresJdbcUrl(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            return databaseUrl;
        }
        if (databaseUrl.startsWith("http://")) {
            return "jdbc:postgresql://" + databaseUrl.substring("http://".length())
                + "/" + DEFAULT_DATABASE_NAME;
        }
        throw new IllegalArgumentException("Unsupported functional-test database URL: " + databaseUrl);
    }

    private static Optional<String> get(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(value -> !value.isBlank());
    }
}
