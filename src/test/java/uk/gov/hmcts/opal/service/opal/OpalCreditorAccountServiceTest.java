package uk.gov.hmcts.opal.service.opal;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.jpa.CreditorAccountTransactions;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpalCreditorAccountServiceTest {

    @Mock
    private CreditorAccountTransactions creditorAccountTransactions;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private OpalCreditorAccountService service;

    @Test
    void testDeleteMinorCreditors_success() {
        // Arrange
        when(creditorAccountTransactions.deleteMinorCreditor(anyLong(), any())).thenReturn(true);

        // Act
        String response = service.deleteCreditorAccount(555L, true, "authHeader");

        // Assert
        assertEquals("{ \"message\": \"Creditor Account '555' deleted\"}", response);
    }

    @Test
    void testDeleteMinorCreditor_success_notDeleted() {
        // Arrange
        when(creditorAccountTransactions.deleteMinorCreditor(anyLong(), any())).thenReturn(false);

        // Act
        String result =
            service.deleteCreditorAccount(777L, false, "authHeaderValue");

        // Assert
        assertEquals("""
                         { "message": "Creditor Account '777' deleted"}""", result);
    }

    @Test
    void testDeleteMinorCreditor_fail_checkExisted() {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
        when(creditorAccountTransactions.deleteMinorCreditor(anyLong(), any()))
            .thenThrow(new EntityNotFoundException("No Creditor Account!"));

        // Act
        EntityNotFoundException error = assertThrows(
            EntityNotFoundException.class, () ->
                service.deleteCreditorAccount(777L, true, "authHeaderValue")
        );

        // Assert
        assertNotNull(error);
        assertEquals("No Creditor Account!", error.getMessage());
    }
}
