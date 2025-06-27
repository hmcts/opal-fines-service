package uk.gov.hmcts.opal.launchdarkly;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureDisabledExceptionTest {

    @Test
    void testConstructor() {
        // Arrange
        String errorMessage = "Feature is disabled";

        // Act
        FeatureDisabledException exception = new FeatureDisabledException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }
}
