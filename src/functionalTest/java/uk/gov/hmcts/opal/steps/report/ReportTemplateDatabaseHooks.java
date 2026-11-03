package uk.gov.hmcts.opal.steps.report;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import uk.gov.hmcts.opal.config.TestEnvironment;

/** Executes the report-template database fixtures for PO-10580 scenarios. */
public class ReportTemplateDatabaseHooks {
    private static final String SETUP_SCRIPT = "/db/report/setup.sql";
    private static final String REPORT_ID = "operational_report_payment";

    private String originalTemplateName;
    private String originalReportParameters;
    private boolean originalCanManuallyCreate;
    private boolean originalDefinitionCaptured;

    @Before("@ReportTemplateDbFixture")
    public void setUpReportDefinition() throws Exception {
        captureOriginalReportDefinition();
        executeScript(SETUP_SCRIPT);
    }

    @After("@ReportTemplateDbFixture")
    public void restoreReportDefinition() throws Exception {
        restoreOriginalReportDefinition();
    }

    private void captureOriginalReportDefinition() throws SQLException {
        String sql = """
            SELECT template_name, report_parameters::text, can_manually_create
            FROM public.reports
            WHERE report_id = ?
            """;

        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, REPORT_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Report definition not found: " + REPORT_ID);
                }
                originalTemplateName = resultSet.getString("template_name");
                originalReportParameters = resultSet.getString("report_parameters");
                originalCanManuallyCreate = resultSet.getBoolean("can_manually_create");
                originalDefinitionCaptured = true;
            }
        }
    }

    private void restoreOriginalReportDefinition() throws SQLException {
        if (!originalDefinitionCaptured) {
            return;
        }

        String sql = """
            UPDATE public.reports
            SET template_name = ?,
                report_parameters = ?::json,
                can_manually_create = ?
            WHERE report_id = ?
            """;

        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, originalTemplateName);
            statement.setString(2, originalReportParameters);
            statement.setBoolean(3, originalCanManuallyCreate);
            statement.setString(4, REPORT_ID);
            statement.executeUpdate();
        }
    }

    private static void executeScript(String script) throws IOException, SQLException {
        try (InputStream resource = ReportTemplateDatabaseHooks.class.getResourceAsStream(script)) {
            if (resource == null) {
                throw new IOException("Database script not found: " + script);
            }
            String sql = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = connection(); Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(
            TestEnvironment.getDatabaseUrl(),
            TestEnvironment.getDatabaseUsername(),
            TestEnvironment.getDatabasePassword());
    }
}
