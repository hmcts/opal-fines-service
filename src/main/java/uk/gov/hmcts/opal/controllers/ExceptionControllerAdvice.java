package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Slf4j(topic = "ExceptionControllerAdvice")
public class ExceptionControllerAdvice {

    public static final String ERROR_MESSAGE = "errorMessage";

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handlePropertyValueException(PropertyValueException pve) {
        log.error(":handlePropertyValueException: {}", pve.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, pve.getMessage(),
            "entity", pve.getEntityName(),
            "property", pve.getPropertyName()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException hmnre) {

        log.error(":handleHttpMessageNotReadableException: {}", hmnre.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, hmnre.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException idaaue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idaaue.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, idaaue.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessResourceUsageException(
        InvalidDataAccessResourceUsageException idarue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idarue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idarue.getRootCause());

        Map<String, String> body = Map.of(
            ERROR_MESSAGE, idarue.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }
}
