package uk.gov.hmcts.opal.versioning;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ApiVersionException extends RuntimeException {
    public ApiVersionException(String message) {
        super(message);
    }
}
