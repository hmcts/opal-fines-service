package uk.gov.hmcts.opal.util;

import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.parseMediaType;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.service.report.FileType;

@SuppressWarnings("unchecked")
public class HttpUtil {

    private static final String NOT_FOUND_MESSAGE =
        """
            { "error": "Not Found", "message": "No resource found at provided URI"}""";
    private static final HttpHeaders HEADERS;

    static {
        HEADERS = new HttpHeaders();
        HEADERS.add("content-type", "application/json");
    }

    private HttpUtil() {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(HEADERS).body(contents);
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);

        if (contents instanceof Versioned versioned) {
            builder.eTag(createETag(versioned));
        }

        return builder.body(contents);
    }

    public static <T> ResponseEntity<T> buildResponse(Versioned versioned, T payload) {
        return ResponseEntity.ok()
            .eTag(createETag(versioned))
            .body(payload);
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


    public static ResponseEntity<Map<String, Object>> buildReportContentResponse(FileType fileType, Object content) {
        if (fileType == JSON) {
            return buildResponse((Map<String, Object>) content);
        }

        MediaType mediaType = switch (fileType) {
            case PDF -> APPLICATION_PDF;
            case XML -> APPLICATION_XML;
            default -> parseMediaType("application/csv");
        };

        return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) ResponseEntity.ok()
            .contentType(mediaType)
            .body((byte[]) content);
    }

}
