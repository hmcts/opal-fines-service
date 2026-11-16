package uk.gov.hmcts.opal.steps.defendantaccount;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Direct database setup and cleanup for disposable APR functional-test fixtures. */
final class AlternatePaymentReferenceFixtureDatabaseClient implements AutoCloseable {

    private static final String INSERT_FIXTURE = """
        INSERT INTO public.alternate_payment_references (
            defendant_account_id, relationship, category, business_unit_id, apr_text,
            created_datetime, updated_datetime, version_number
        )
        SELECT defendant_account_id, ?::t_apr_relationship_enum, ?::t_apr_category_enum,
               business_unit_id, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
        FROM public.defendant_accounts
        WHERE defendant_account_id = ?
        RETURNING alternate_payment_reference_id
        """;

    private static final String DELETE_FOR_DEFENDANT_ACCOUNT = """
        DELETE FROM public.alternate_payment_references
        WHERE defendant_account_id = ?
        """;

    private final Connection connection;

    AlternatePaymentReferenceFixtureDatabaseClient() {
        String url = environment("FUNCTIONAL_TEST_FINES_DB_URL")
            .orElseGet(() -> "jdbc:postgresql://"
                + environment("OPAL_FINES_DB_HOST").orElse("localhost") + ":"
                + environment("OPAL_FINES_DB_PORT").orElse("5432") + "/"
                + environment("OPAL_FINES_DB_NAME").orElse("opal-fines-db"));
        String username = environment("FUNCTIONAL_TEST_FINES_DB_USERNAME")
            .orElseGet(() -> environment("OPAL_FINES_DB_USERNAME").orElse("opal-db-user"));
        String password = environment("FUNCTIONAL_TEST_FINES_DB_PASSWORD")
            .orElseGet(() -> environment("OPAL_FINES_DB_PASSWORD").orElse("opal-db-password"));
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not connect to the functional-test Fines database", exception);
        }
    }

    long createFixture(String defendantAccountId, String relationship, String category, String aprText) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_FIXTURE)) {
            statement.setString(1, relationship);
            statement.setString(2, category);
            statement.setString(3, aprText);
            statement.setLong(4, Long.parseLong(defendantAccountId));
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("Defendant account " + defendantAccountId
                        + " was not found in the functional-test Fines database");
                }
                return result.getLong("alternate_payment_reference_id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create the APR functional-test fixture", exception);
        }
    }

    void deleteForDefendantAccount(String defendantAccountId) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_FOR_DEFENDANT_ACCOUNT)) {
            statement.setLong(1, Long.parseLong(defendantAccountId));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not remove APRs for functional-test defendant account "
                + defendantAccountId, exception);
        }
    }

    private static Optional<String> environment(String name) {
        return Optional.ofNullable(System.getenv(name)).filter(value -> !value.isBlank());
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not close the functional-test Fines database connection",
                exception);
        }
    }
}
