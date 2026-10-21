package uk.gov.hmcts.opal.exception;

public class PdplLoggingException extends RuntimeException {

    public PdplLoggingException(String message) {
        super(message);
    }

    public PdplLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}
