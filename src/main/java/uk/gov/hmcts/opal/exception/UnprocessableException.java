package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class UnprocessableException extends RuntimeException {

    private final String detailedReason;

    public UnprocessableException(String detailedReason) {
        this.detailedReason = detailedReason;
    }

}
