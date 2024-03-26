package uk.gov.hmcts.opal.scheduler.exception;

public class JobException extends RuntimeException {

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
