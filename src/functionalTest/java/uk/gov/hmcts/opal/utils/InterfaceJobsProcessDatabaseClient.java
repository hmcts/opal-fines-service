package uk.gov.hmcts.opal.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Reads deployed database state for the PO-2593 functional tests.
 */
public final class InterfaceJobsProcessDatabaseClient {

    private static final String DB_HOST = "OPAL_FINES_DB_HOST";
    private static final String DB_PORT = "OPAL_FINES_DB_PORT";
    private static final String DB_NAME = "OPAL_FINES_DB_NAME";
    private static final String DB_OPTIONS = "OPAL_FINES_DB_OPTIONS";
    private static final String DB_USERNAME = "OPAL_FINES_DB_USERNAME";
    private static final String DB_PASSWORD = "OPAL_FINES_DB_PASSWORD";

    /**
     * Loads the persisted state of an interface job and its files.
     *
     * @param jobId interface job identifier.
     * @return persisted job state.
     * @throws SQLException when the database cannot be queried.
     */
    public JobState loadJobState(long jobId) throws SQLException {
        String sql = "SELECT j.status::text, j.started_datetime, "
            + "COALESCE(bool_and(f.override_inhibits), false) "
            + "FROM interface_jobs j LEFT JOIN interface_files f "
            + "ON f.interface_job_id = j.interface_job_id "
            + "WHERE j.interface_job_id = ? GROUP BY j.interface_job_id, j.status, j.started_datetime";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, jobId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new AssertionError("No interface job found for " + jobId);
                }
                return new JobState(resultSet.getString(1), resultSet.getTimestamp(2), resultSet.getBoolean(3));
            }
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
            databaseUrl(),
            requiredEnvironmentVariable(DB_USERNAME),
            requiredEnvironmentVariable(DB_PASSWORD));
    }

    private String databaseUrl() {
        return "jdbc:postgresql://%s:%s/%s%s".formatted(
            requiredEnvironmentVariable(DB_HOST),
            requiredEnvironmentVariable(DB_PORT),
            requiredEnvironmentVariable(DB_NAME),
            optionalEnvironmentVariable(DB_OPTIONS));
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " before running PO-2593 functional tests");
        }
        return value;
    }

    private String optionalEnvironmentVariable(String name) {
        return System.getenv().getOrDefault(name, "");
    }

    /**
     * Persisted interface-job state used by the functional assertions.
     *
     * @param status database status value.
     * @param startedDatetime database start timestamp.
     * @param overrideInhibits combined override-inhibits value for the job's files.
     */
    public record JobState(String status, Timestamp startedDatetime, boolean overrideInhibits) {
    }
}
