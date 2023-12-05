package uk.gov.hmcts.opal.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;

public interface OpalApiError {

    String getErrorTypePrefix();

    String getErrorTypeNumeric();

    HttpStatus getHttpStatus();

    String getTitle();

    default URI getType() {
        return URI.create(
            String.format("%s_%s", getErrorTypePrefix(), getErrorTypeNumeric())
        );
    }

}
