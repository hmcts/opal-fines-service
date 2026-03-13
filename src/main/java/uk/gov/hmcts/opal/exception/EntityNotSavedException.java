package uk.gov.hmcts.opal.exception;

public class EntityNotSavedException extends RuntimeException {

    public EntityNotSavedException(String message, Throwable cause) {
        super(message, cause);
    }

}
