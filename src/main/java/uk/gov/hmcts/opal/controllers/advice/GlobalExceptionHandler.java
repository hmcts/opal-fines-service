package uk.gov.hmcts.opal.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AccessTokenService tokenService;

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<String> handleFeatureDisabledException(FeatureDisabledException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({PermissionNotAllowedException.class, AccessDeniedException.class})
    public ResponseEntity<String> handlePermissionNotAllowedException(Exception ex,
                                                                      HttpServletRequest request) {
        String authorization = request.getHeader(AUTH_HEADER);
        String preferredName = extractPreferredUsername(authorization, tokenService);
        String message = String.format("For user %s, %s", preferredName, ex.getMessage());
        log.warn(message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }
}
