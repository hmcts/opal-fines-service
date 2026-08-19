package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;

class TransientFailureHelperTest {

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
}
