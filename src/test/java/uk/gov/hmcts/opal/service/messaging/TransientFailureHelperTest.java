package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(transientFailureHelper.isTransientFailure(failure)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nonTransientFailures")
    void isTransientFailure_returnsFalseForNonTransientFailures(RuntimeException failure) {
        assertThat(transientFailureHelper.isTransientFailure(failure)).isFalse();
    }

    @Test
    void isTransientFailure_returnsTrueForLockNotAvailableSqlState() {
        PSQLException psqlException = lockNotAvailableException();

        assertThat(transientFailureHelper.isTransientFailure(new JpaSystemException(
            new RuntimeException("wrap", psqlException)))).isTrue();
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
            new RuntimeException(serializationFailure)
        );
    }

    private static Stream<RuntimeException> nonTransientFailures() {
        PSQLException syntaxFailure = new PSQLException("syntax", PSQLState.SYNTAX_ERROR);
        PSQLException unexpectedFailure = new PSQLException("unexpected", PSQLState.UNEXPECTED_ERROR);

        return Stream.of(
            new UnprocessableException("validation failed"),
            new IllegalArgumentException("bad request"),
            new TransactionSystemException("tx", syntaxFailure),
            new JpaSystemException(new RuntimeException("plain")),
            new RuntimeException(unexpectedFailure)
        );
    }

    private static PSQLException lockNotAvailableException() {
        return new PSQLException("locked", PSQLState.UNKNOWN_STATE) {
            @Override
            public String getSQLState() {
                return "55P03";
            }
        };
    }
}
