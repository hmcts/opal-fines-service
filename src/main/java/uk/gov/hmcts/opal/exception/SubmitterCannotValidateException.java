package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class SubmitterCannotValidateException extends RuntimeException {

    private final String submittedBy;
    private final String validatedBy;

    public SubmitterCannotValidateException(String message, String submittedBy, String validatedBy) {
        super(message);
        this.submittedBy = submittedBy;
        this.validatedBy = validatedBy;
    }
}
