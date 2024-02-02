package uk.gov.hmcts.opal.util;

import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseUtil {

    public static <T> ResponseEntity<List<T>> buildResponse(List<T> contents) {
        if (contents == null || contents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(contents);
    }
}
