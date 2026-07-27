package uk.gov.hmcts.opal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class LegacyGatewayException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String statusText;

    public LegacyGatewayException(HttpStatusCode statusCode, String statusText, Throwable cause) {
        super(statusText, cause);
        this.statusCode = statusCode;
        this.statusText = statusText;
    }
}
