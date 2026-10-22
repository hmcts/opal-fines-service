package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class RequestValidationException extends RuntimeException {

    public RequestValidationException(String message) {
        super(message);
    }
}
