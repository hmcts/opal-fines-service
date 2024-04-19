package uk.gov.hmcts.opal.controllers.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<String> handleFeatureDisabledException(FeatureDisabledException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(PermissionNotAllowedException.class)
    public ResponseEntity<String> handlePermissionNotAllowedException(PermissionNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
