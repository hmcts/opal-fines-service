package uk.gov.hmcts.opal.exception;

public class SubmitterCannotDeleteException extends RuntimeException {

    public SubmitterCannotDeleteException(String message) {
        super(message);
    }
}
