package uk.gov.hmcts.opal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String DEFAULT_DB_HOST = "localhost";
    private static final String DEFAULT_DB_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "opal-fines-db";
    private static final String DEFAULT_DB_USERNAME = "opal-db-user";
    private static final String DEFAULT_DB_PASSWORD = "opal-db-password";
    private static final String DEFAULT_DB_OPTIONS = "";

    static Logger log = LoggerFactory.getLogger(DBConnection.class.getName());

    public Connection getPostgresConnection() {
        Connection conn = null;

        log.info("Making postgres connection");
        String host = getEnvOrDefault("OPAL_FINES_DB_HOST", DEFAULT_DB_HOST);
        String port = getEnvOrDefault("OPAL_FINES_DB_PORT", DEFAULT_DB_PORT);
        String database = getEnvOrDefault("OPAL_FINES_DB_NAME", DEFAULT_DB_NAME);
        String username = getEnvOrDefault("OPAL_FINES_DB_USERNAME", DEFAULT_DB_USERNAME);
        String password = getEnvOrDefault("OPAL_FINES_DB_PASSWORD", DEFAULT_DB_PASSWORD);
        String options = getEnvOrDefault("OPAL_FINES_DB_OPTIONS", DEFAULT_DB_OPTIONS);
        String url = String.format("jdbc:postgresql://%s:%s/%s%s", host, port, database, options);

        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);
        try {
            conn = DriverManager.getConnection(url, props);
            log.info("Successfully made to postgres database");
        } catch (SQLException e) {
            log.info("Error: " + e);
        }
        return conn;
    }

    private String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
