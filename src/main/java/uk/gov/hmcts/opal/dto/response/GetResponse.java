package uk.gov.hmcts.opal.dto.response;

/** Standard contract for versioned GET responses. */

public interface GetResponse<T> {
    T getData();

    Long getVersion();
}
