package uk.gov.hmcts.opal.service.messaging;

import com.azure.storage.blob.models.BlobStorageException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
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

    /**
     * Inspects Till allocation failure causes; unrecognised failures are treated as non-transient.
     */
    public boolean isTransientTillAllocationFailure(RuntimeException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof UnprocessableException unprocessable) {
                return unprocessable.isRetriable();
            }
            if (cause instanceof BlobStorageException blobException) {
                return isTransientBlobFailure(blobException);
            }
            if (cause instanceof SQLException sqlException) {
                String state = sqlException.getSQLState();
                if (isConnectionExceptionSqlState(state) || isTransientSqlState(state)) {
                    return true;
                }
            }
            if (isTransientInfrastructureFailure(cause)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean isConnectionExceptionSqlState(String state) {
        // SQLSTATE class 08 identifies connection exceptions.
        return state != null && state.startsWith("08");
    }

    private static boolean isTransientInfrastructureFailure(Throwable cause) {
        return cause instanceof TransientDataAccessException || cause instanceof RecoverableDataAccessException
            || cause instanceof DataAccessResourceFailureException
            || cause instanceof SocketException || cause instanceof UnknownHostException
            || cause instanceof SocketTimeoutException || cause instanceof TimeoutException;
    }

    private static boolean isTransientBlobFailure(BlobStorageException exception) {
        int status = exception.getStatusCode();
        return status == HttpStatus.REQUEST_TIMEOUT.value() || status == HttpStatus.TOO_MANY_REQUESTS.value()
            || status >= 500 && status < 600
            && status != HttpStatus.NOT_IMPLEMENTED.value() && status != HttpStatus.HTTP_VERSION_NOT_SUPPORTED.value();
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
