package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permissions;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.client.service.UserStateClientService;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TempUserServiceControllerTest {

    @Mock
    private UserStateClientService userStateClientService;

    @InjectMocks
    private TempUserServiceController tempUserServiceController;


    @Test
    void testGetDraftAccount_Success() {
        // Arrange
        UserState state = UserStateUtil.permissionUser((short)77, Permissions.ACCOUNT_ENQUIRY);
        when(userStateClientService.getUserState(any())).thenReturn(Optional.of(state));

        // Act
        Optional<UserState> userState = tempUserServiceController.getUserState(0L);

        // Assert
        assertTrue(userState.isPresent());
        assertEquals("normal@users.com", userState.get().getUserName());
        assertEquals(1L, userState.get().getUserId());
    }

}
