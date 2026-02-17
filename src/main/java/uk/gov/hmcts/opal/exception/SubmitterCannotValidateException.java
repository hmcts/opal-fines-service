package uk.gov.hmcts.opal.exception;

public class SubmitterCannotValidateException extends RuntimeException {

    public SubmitterCannotValidateException(String message) {
        super(message);
    }
}
