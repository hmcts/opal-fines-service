package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class UnprocessableException extends RuntimeException {

    private final String detailedReason;

    private final boolean retriable;

    public UnprocessableException(String detailedReason) {
        this(detailedReason, false);
    }

    public UnprocessableException(String detailedReason, boolean retriable) {
        this.detailedReason = detailedReason;
        this.retriable = retriable;
    }

}
