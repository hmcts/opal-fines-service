package uk.gov.hmcts.opal.controllers;

import org.hibernate.PropertyValueException;
import org.htmlunit.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ExceptionControllerAdviceTest {

    @InjectMocks
    private ExceptionControllerAdvice exceptionControllerAdvice;

    @Test
    void handlePropertyValueException() {
        // Arrange
        PropertyValueException pve = new PropertyValueException("A Test Message", "DraftAccountEntity", "account");
        // Act
        ResponseEntity<Map<String, String>> response = exceptionControllerAdvice.handlePropertyValueException(pve);
        // Assert
        assertEquals(HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException hmnre = new HttpMessageNotReadableException("A Test Message");
        // Act
        ResponseEntity<Map<String, String>> response = exceptionControllerAdvice
            .handleHttpMessageNotReadableException(hmnre);
        // Assert
        assertEquals(HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleInvalidDataAccessApiUsageException() {
        // Arrange
        InvalidDataAccessApiUsageException idaaue = new InvalidDataAccessApiUsageException("A Test Message");
        // Act
        ResponseEntity<Map<String, String>> response = exceptionControllerAdvice
            .handleInvalidDataAccessApiUsageException(idaaue);
        // Assert
        assertEquals(HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleInvalidDataAccessResourceUsageException() {
        // Arrange
        InvalidDataAccessResourceUsageException idarue = new InvalidDataAccessResourceUsageException("A Test Message");
        // Act
        ResponseEntity<Map<String, String>> response = exceptionControllerAdvice
            .handleInvalidDataAccessResourceUsageException(idarue);
        // Assert
        assertEquals(HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

}
