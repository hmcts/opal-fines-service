package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;

@ExtendWith(MockitoExtension.class)
class TransientFailureHelperTest {

    @Mock
    private HttpResponse response;

    private final TransientFailureHelper transientFailureHelper = new TransientFailureHelper();

    @ParameterizedTest
    @MethodSource("transientFailures")
    void isTransientFailure_returnsTrueForTransientFailures(RuntimeException failure) {
        // Arrange
        RuntimeException transientFailure = failure;

        // Act
        boolean result = transientFailureHelper.isTransientFailure(transientFailure);

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonTransientFailures")
    void isTransientFailure_returnsFalseForNonTransientFailures(RuntimeException failure) {
        // Arrange
        RuntimeException nonTransientFailure = failure;

        // Act
        boolean result = transientFailureHelper.isTransientFailure(nonTransientFailure);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isTransientFailure_returnsTrueForLockNotAvailableSqlState() {
        // Arrange
        PSQLException psqlException = lockNotAvailableException();

        // Act
        boolean result = transientFailureHelper.isTransientFailure(new JpaSystemException(
            new RuntimeException("wrap", psqlException)));

        // Assert
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("psqlConnectionFailures")
    void isTransientFailure_returnsTrueForConnectionExceptionsNestedUnderPsql(RuntimeException failure) {
        // Arrange
        RuntimeException nestedPsqlFailure = failure;

        // Act
        boolean result = transientFailureHelper.isTransientFailure(nestedPsqlFailure);

        // Assert
        assertThat(result).isTrue();
    }

    private static Stream<RuntimeException> transientFailures() {
        PSQLException serializationFailure = new PSQLException("serial", PSQLState.SERIALIZATION_FAILURE);
        PSQLException deadlockFailure = new PSQLException("deadlock", PSQLState.DEADLOCK_DETECTED);

        return Stream.of(
            new UnprocessableException("validation failed", true),
            new ReportGenerationException("report failed", new IllegalArgumentException("blob container missing")),
            new DataAccessResourceFailureException("database unavailable"),
            new QueryTimeoutException("database timeout"),
            new TransactionSystemException("tx", deadlockFailure),
            new JpaSystemException(new RuntimeException("wrap", serializationFailure)),
            new RuntimeException(serializationFailure));
    }

    private static Stream<RuntimeException> nonTransientFailures() {
        PSQLException syntaxFailure = new PSQLException("syntax", PSQLState.SYNTAX_ERROR);
        PSQLException unexpectedFailure = new PSQLException("unexpected", PSQLState.UNEXPECTED_ERROR);

        return Stream.of(
            new UnprocessableException("validation failed"),
            new IllegalArgumentException("bad request"),
            new TransactionSystemException("tx", syntaxFailure),
            new JpaSystemException(new RuntimeException("plain")),
            new RuntimeException(unexpectedFailure));
    }

    private static Stream<RuntimeException> psqlConnectionFailures() {
        return Stream.of(
            new RuntimeException(connectionExceptionPsql()),
            new RuntimeException(unknownHostExceptionPsql()));
    }

    private static PSQLException lockNotAvailableException() {
        return new PSQLException("locked", PSQLState.UNKNOWN_STATE) {
            @Override
            public String getSQLState() {
                return "55P03";
            }
        };
    }

    private static PSQLException connectionExceptionPsql() {
        return new PSQLException("locked", PSQLState.UNKNOWN_STATE) {
            @Override
            public Throwable getCause() {
                return new ConnectException("connect failed");
            }
        };
    }

    private static PSQLException unknownHostExceptionPsql() {
        return new PSQLException("locked", PSQLState.UNKNOWN_STATE) {
            @Override
            public Throwable getCause() {
                return new UnknownHostException("unknown host");
            }
        };
    }

    @ParameterizedTest
    @MethodSource("transientAllocationFailures")
    void recognisesTransientAllocationFailures(RuntimeException failure) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(failure)).isTrue();
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(
            new ReportGenerationException("Report failed", failure))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonTransientAllocationFailures")
    void rejectsKnownNonTransientFailures(RuntimeException failure) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(failure)).isFalse();
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(
            new ReportGenerationException("Report failed", failure))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"08006", "40001", "40P01", "55P03"})
    void retriesTransientDatabaseCodes(String state) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(
            new RuntimeException(new SQLException("DB failure", state)))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"22003", "23503", "42601", "P4101", "P4106", "P4115"})
    void rejectsNonTransientDatabaseCodes(String state) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(
            new RuntimeException(new SQLException("DB failure", state)))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 500, 502, 503, 504})
    void retriesBlobServiceFailures(int status) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(blobFailure(status))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 409, 501, 505})
    void rejectsNonTransientBlobFailures(int status) {
        assertThat(transientFailureHelper.isTransientTillAllocationFailure(
            new ReportGenerationException("Report failed", blobFailure(status))))
            .isFalse();
    }

    private BlobStorageException blobFailure(int status) {
        when(response.getStatusCode()).thenReturn(status);
        return new BlobStorageException("Blob failed", response, null);
    }

    private static Stream<RuntimeException> transientAllocationFailures() {
        TransactionSystemException deadlock = new TransactionSystemException("Transaction failed",
            new PSQLException("Deadlock", PSQLState.DEADLOCK_DETECTED));
        JpaSystemException serializationFailure = new JpaSystemException(new RuntimeException("Database failed",
            new PSQLException("Serialization failure", PSQLState.SERIALIZATION_FAILURE)));
        RuntimeException nestedReportTimeout = new RuntimeException(
            new ReportGenerationException("Report failed", new QueryTimeoutException("Timeout")));

        return Stream.of(
            new RuntimeException(new ConnectException()),
            new RuntimeException(new SocketException("Connection reset")),
            new RuntimeException(new UnknownHostException()),
            new RuntimeException(new SocketTimeoutException()),
            new RuntimeException(new TimeoutException()),
            new RecoverableDataAccessException("Recoverable"),
            nestedReportTimeout,
            new DataAccessResourceFailureException("DB unavailable"),
            new QueryTimeoutException("Timeout"),
            deadlock,
            serializationFailure,
            new UnprocessableException("Retry", true));
    }

    private static Stream<RuntimeException> nonTransientAllocationFailures() {
        TransactionSystemException transactionSyntaxFailure = new TransactionSystemException("Transaction failed",
            new PSQLException("Invalid SQL", PSQLState.SYNTAX_ERROR));
        JpaSystemException jpaSyntaxFailure = new JpaSystemException(new RuntimeException("Database failed",
            new PSQLException("Invalid SQL", PSQLState.SYNTAX_ERROR)));

        return Stream.of(
            new RuntimeException("Unknown"),
            new ReportGenerationException("Unknown", null),
            new RuntimeException(new IOException("I/O failure")),
            new IllegalArgumentException("Invalid"),
            new EntityNotFoundException("Missing"),
            transactionSyntaxFailure,
            jpaSyntaxFailure,
            new UnprocessableException("Invalid", false));
    }
}
