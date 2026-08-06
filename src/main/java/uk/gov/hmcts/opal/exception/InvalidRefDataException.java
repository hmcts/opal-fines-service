package uk.gov.hmcts.opal.exception;

public class InvalidRefDataException extends RuntimeException {

    public InvalidRefDataException(String msg) {
        super(msg);
    }

    public InvalidRefDataException(Throwable t) {
        super(t);
    }

    public InvalidRefDataException(String message, Throwable t) {
        super(message, t);
    }
}
