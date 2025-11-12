package uk.gov.hmcts.opal.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

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

    /* Create a 'default' response with a HTTP Status of 'OK'. */
    public static <T> ResponseEntity<T> buildResponse(T contents) {
        return buildResponse(contents, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> buildResponse(T contents, HttpStatus status) {
        if (contents == null) {
            contents = (T) NOT_FOUND_MESSAGE;
            return new ResponseEntity<>(contents, HEADERS, HttpStatus.NOT_FOUND);
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);

        if (contents instanceof Versioned versioned) {
            builder.eTag(createETag(versioned));
        }

        return builder.body(contents);
    }

    /* Create a response with a HTTP Status of 'CREATED'. */
    public static <T> ResponseEntity<T> buildCreatedResponse(T contents) {
        return buildResponse(contents, HttpStatus.CREATED);
    }

    public static String extractPreferredUsername(String authorization, AccessTokenService tokenService) {
        return Optional.ofNullable(authorization)
            .map(tokenService::extractPreferredUsername)
            .orElse(null);
    }

}
