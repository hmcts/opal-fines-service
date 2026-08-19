package uk.gov.hmcts.opal.service.messaging;

import java.net.ConnectException;
import java.net.UnknownHostException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;

@Component
public class TransientFailureHelper {

    public boolean isTransientFailure(RuntimeException ex) {
        if (ex instanceof ReportGenerationException) {
            return true;
        }
        if (ex instanceof UnprocessableException unprocessableException) {
            return unprocessableException.isRetriable();
        }
        if (ex instanceof DataAccessResourceFailureException || ex instanceof QueryTimeoutException) {
            return true;
        }
        if (ex instanceof TransactionSystemException || ex instanceof JpaSystemException) {
            PSQLException psqlException = findPsqlException(ex);
            return psqlException != null
                && (isTransientConnectionFailure(psqlException) || isTransientSqlState(psqlState(psqlException)));
        }

        PSQLException psqlException = findPsqlException(ex);
        if (psqlException != null) {
            return isTransientConnectionFailure(psqlException) || isTransientSqlState(psqlState(psqlException));
        }

        return false;
    }

    private static boolean isTransientConnectionFailure(PSQLException psqlException) {
        return psqlException.getCause() instanceof ConnectException
            || psqlException.getCause() instanceof UnknownHostException;
    }

    private static PSQLException findPsqlException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof PSQLException psqlException) {
                return psqlException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static String psqlState(Throwable throwable) {
        if (throwable instanceof PSQLException psqlException) {
            return psqlException.getSQLState();
        }
        return null;
    }

    private static boolean isTransientSqlState(String state) {
        if (state == null) {
            return false;
        }
        return state.equals("40001")
            || state.equals("40P01")
            || state.equals("55P03");
    }
}
