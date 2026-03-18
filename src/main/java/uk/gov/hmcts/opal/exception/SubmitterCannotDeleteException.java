package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class SubmitterCannotDeleteException extends RuntimeException {

    private final String submittedBy;
    private final String deletedBy;

    public SubmitterCannotDeleteException(String message, String submittedBy, String deletedBy) {
        super(message);
        this.submittedBy = submittedBy;
        this.deletedBy = deletedBy;
    }
}
