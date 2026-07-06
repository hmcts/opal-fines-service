package uk.gov.hmcts.opal.exception;

import java.util.List;

public class UnsupportedContentTypeException extends RuntimeException {

    public UnsupportedContentTypeException(String resourceName, String requestedContentType,
        List<String> supportedContentTypes) {
        super("Content type " + requestedContentType + " is not supported for " + resourceName
            + ". Supported content types: " + String.join(", ", supportedContentTypes));
    }
}
