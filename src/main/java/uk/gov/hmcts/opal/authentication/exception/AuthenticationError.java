package uk.gov.hmcts.opal.authentication.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.exception.OpalApiError;

@Getter
@RequiredArgsConstructor
public enum AuthenticationError implements OpalApiError {

    FAILED_TO_OBTAIN_ACCESS_TOKEN(
        "100",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to obtain access token"
    ),

    FAILED_TO_VALIDATE_ACCESS_TOKEN(
        "101",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to validate access token"
    ),

    FAILED_TO_PARSE_ACCESS_TOKEN(
        "102",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to parse access token"
    ),

    FAILED_TO_OBTAIN_AUTHENTICATION_CONFIG("103",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to find authentication configuration");

    private static final String ERROR_TYPE_PREFIX = "AUTHENTICATION";

    private final String errorTypeNumeric;
    private final HttpStatus httpStatus;
    private final String title;

    @Override
    public String getErrorTypePrefix() {
        return ERROR_TYPE_PREFIX;
    }

}
