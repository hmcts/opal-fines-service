package uk.gov.hmcts.opal.authorisation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    @InjectMocks
    private AuthorisationService authorisationService;

    @Test
    void getAuthorisation_ReturnsUserState_WhenUserFound() {
        // Arrange
        String emailAddress = "test@example.com";

        // Act
        Optional<UserState> result = authorisationService.getAuthorisation(emailAddress);

        // Assert
        assertTrue(result.isPresent());
        // Add more assertions as needed to validate the returned UserState
    }
}
