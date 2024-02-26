package uk.gov.hmcts.opal.authorisation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    @Mock
    UserService userService;

    @InjectMocks
    private AuthorisationService authorisationService;

    @Test
    void getAuthorisation_ReturnsUserState_WhenUserFound() {
        // Arrange
        String emailAddress = "test@example.com";
        UserState userState = UserState.builder().userId("JS001").userName("John Smith").build();
        when(userService.getUserStateByUsername(any())).thenReturn(userState);

        // Act
        UserState result = authorisationService.getAuthorisation(emailAddress);

        // Assert
        assertEquals(userState, result);
        // Add more assertions as needed to validate the returned UserState
    }
}
