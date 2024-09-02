package uk.gov.hmcts.opal.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class HttpUtil {

    private static final String NOT_FOUND_MESSAGE =
        """
    { "error": "Not Found", "message": "No resource found at provided URI"}""";

    private static final MultiValueMap<String, String> HEADERS;

    static {
        HEADERS = new LinkedMultiValueMap<>();
        HEADERS.add("content-type", "application/json");
    }

    public static <T> ResponseEntity<List<T>> buildResponse(List<T> contents) {
        //return list even if empty
        return ResponseEntity.ok(contents);
    }

    public static <T> ResponseEntity<T> buildResponse(T contents) {
        if (contents == null) {
            contents = (T) NOT_FOUND_MESSAGE;
            return new ResponseEntity<>(contents, HEADERS, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(contents);
    }

    public static <T> ResponseEntity<T> buildCreatedResponse(T contents) {
        if (contents == null) {
            contents = (T) NOT_FOUND_MESSAGE;
            return new ResponseEntity<>(contents, HEADERS, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(contents, HttpStatus.CREATED);
    }

    public static String extractPreferredUsername(String authorization, AccessTokenService tokenService) {
        return Optional.ofNullable(authorization)
            .map(tokenService::extractPreferredUsername)
            .orElse(null);
    }

}
