package uk.gov.hmcts.opal.controllers.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.exception.OpalApiError;

@Getter
@RequiredArgsConstructor
public enum DataLookUpError implements OpalApiError {

    NO_CONTENT_FOUND_AT_URI("100",
        HttpStatus.NOT_FOUND,
        "The Resource identified by the provided URI cannot be found");

    private static final String ERROR_TYPE_PREFIX = "DATA_LOOK_UP";

    private final String errorTypeNumeric;
    private final HttpStatus httpStatus;
    private final String title;

    @Override
    public String getErrorTypePrefix() {
        return ERROR_TYPE_PREFIX;
    }

}
