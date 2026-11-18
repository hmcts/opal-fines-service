package uk.gov.hmcts.opal.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
public class VersionedResponse<T> {

    private final T data;
    private final Long version;

    public String getVersionStr() {
        return version == null ? null : version.toString();
    }

    public ResponseEntity<T> toResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status)
            .eTag(getVersionStr())
            .body(data);
    }

}
