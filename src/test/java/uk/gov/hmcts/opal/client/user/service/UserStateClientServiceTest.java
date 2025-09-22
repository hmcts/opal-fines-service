package uk.gov.hmcts.opal.client.user.service;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.client.UserClient;
import uk.gov.hmcts.opal.common.user.client.dto.UserStateDto;
import uk.gov.hmcts.opal.common.user.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.client.service.UserStateClientService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStateClientServiceTest {

    @Mock
    private UserClient userClient;

    @Spy
    private UserStateMapper userStateMapper = Mappers.getMapper(UserStateMapper.class);

    @InjectMocks
    private UserStateClientService userStateClientService;

    @Test
    void testCheckForAuthorisedUser_success() {
        // Arrange
        UserStateDto dto = UserStateDto.builder()
            .username("HMCTS User")
            .userId(777L)
            .build();
        when(userClient.getUserStateById(any())).thenReturn(dto);

        // Act
        Optional<UserState> userState = userStateClientService.getUserState(0L);

        // Assert
        assertTrue(userState.isPresent());
        assertEquals("HMCTS User", userState.get().getUserName());
        assertEquals(777L, userState.get().getUserId());
    }

    @Test
    void testCheckForAuthorisedUser_notFound() {
        // Arrange
        Request request = Mockito.mock(Request.class);
        when(userClient.getUserStateById(any())).thenThrow(new FeignException.NotFound(null, request, null, null));

        // Act
        Optional<UserState> userState = userStateClientService.getUserState(0L);

        // Assert
        assertTrue(userState.isEmpty());
    }

    @Test
    void testGetUserStateByAuthenticatedUser() {
        // Arrange
        UserStateDto dto = UserStateDto.builder()
            .username("HMCTS User")
            .userId(777L)
            .build();
        when(userClient.getUserStateById(any())).thenReturn(dto);

        // Act
        Optional<UserState> userState = userStateClientService.getUserStateByAuthenticatedUser(userStateClientService);

        // Assert
        assertTrue(userState.isPresent());
        assertEquals("HMCTS User", userState.get().getUserName());
        assertEquals(777L, userState.get().getUserId());
    }
}
