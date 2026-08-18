package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class DefendantAccountNotFoundException extends RuntimeException {

    private final long defendantAccountId;

    public DefendantAccountNotFoundException(long defendantAccountId) {
        super("Defendant account not found with id: " + defendantAccountId);
        this.defendantAccountId = defendantAccountId;
    }
}
