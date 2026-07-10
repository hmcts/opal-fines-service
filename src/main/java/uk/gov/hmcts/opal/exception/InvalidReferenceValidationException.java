package uk.gov.hmcts.opal.exception;

public class InvalidReferenceValidationException extends RuntimeException {

    public InvalidReferenceValidationException(String msg) {
        super(msg);
    }

    public InvalidReferenceValidationException(Throwable t) {
        super(t);
    }

    public InvalidReferenceValidationException(String message, Throwable t) {
        super(message, t);
    }

}
