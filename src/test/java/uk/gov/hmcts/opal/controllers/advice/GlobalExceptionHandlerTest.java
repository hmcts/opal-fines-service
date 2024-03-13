package uk.gov.hmcts.opal.controllers.advice;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private FeatureDisabledException exception;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    public void handleFeatureDisabledException_ReturnsMethodNotAllowed() {
        // Arrange
        String errorMessage = "Feature is disabled";
        when(exception.getMessage()).thenReturn(errorMessage);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleFeatureDisabledException(exception);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}
