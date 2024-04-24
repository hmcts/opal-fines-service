package uk.gov.hmcts.opal.authentication.exception;

import lombok.Getter;

@Getter
public class MissingRequestHeaderException extends RuntimeException {

    private final String headerName;

    public MissingRequestHeaderException(String headerName) {
        super("Missing request header named: %s".formatted(headerName));
        this.headerName = headerName;
    }
}
