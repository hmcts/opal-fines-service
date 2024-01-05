package uk.gov.hmcts.opal.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@SuppressWarnings("PMD.NullAssignment")
public class OpalApiException extends RuntimeException {

    private final OpalApiError error;
    private final String detail;
    private final HashMap<String, Object> customProperties = new HashMap<>();

    public OpalApiException(OpalApiError error) {
        super(error.getTitle());

        this.error = error;
        this.detail = null;
    }

    public OpalApiException(OpalApiError error, Throwable throwable) {
        super(error.getTitle(), throwable);

        this.error = error;
        this.detail = null;
    }

    public OpalApiException(OpalApiError error, String detail) {
        super(String.format("%s. %s", error.getTitle(), detail));

        this.error = error;
        this.detail = detail;
    }

    public OpalApiException(OpalApiError error, Map<String, Object> customProperties) {
        super(error.getTitle());

        this.error = error;
        this.detail = null;
        this.customProperties.putAll(customProperties);
    }

    public OpalApiException(OpalApiError error, String detail, Map<String, Object> customProperties) {
        super(String.format("%s. %s", error.getTitle(), detail));

        this.error = error;
        this.detail = detail;
        this.customProperties.putAll(customProperties);
    }

    public OpalApiException(OpalApiError error, String detail, Throwable throwable) {
        super(String.format("%s. %s", error.getTitle(), detail), throwable);

        this.error = error;
        this.detail = detail;
    }

}
