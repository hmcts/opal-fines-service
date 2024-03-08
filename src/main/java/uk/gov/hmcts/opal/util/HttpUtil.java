package uk.gov.hmcts.opal.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;

public class HttpUtil {

    public static <T> ResponseEntity<List<T>> buildResponse(List<T> contents) {
        if (contents == null || contents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(contents);
    }

    public static <T> ResponseEntity<T> buildResponse(T contents) {
        if (contents == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(contents);
    }

    public static <T> ResponseEntity<T> buildCreatedResponse(T contents) {
        if (contents == null) {
            return ResponseEntity.noContent().build();
        }

        return new ResponseEntity<>(contents, HttpStatus.CREATED);
    }

    public static String extractPreferredUsername(HttpServletRequest request, AccessTokenService tokenService) {
        return Optional.ofNullable(request.getHeader(AUTH_HEADER))
            .map(tokenService::extractPreferredUsername)
            .orElse(null);
    }

}
