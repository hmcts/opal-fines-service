package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.opal.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetUser_Success() {
        // Arrange
        UserEntity entity = UserEntity.builder().build();

        when(userService.getUser(anyString())).thenReturn(entity);

        // Act
        ResponseEntity<UserEntity> response = userController.getUserById("1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(userService, times(1)).getUser(anyString());
    }

    @Test
    void testSearchUsers_Success() {
        // Arrange
        UserEntity entity = UserEntity.builder().build();
        List<UserEntity> userList = List.of(entity);

        when(userService.searchUsers(any())).thenReturn(userList);

        // Act
        UserSearchDto searchDto = UserSearchDto.builder().build();
        ResponseEntity<List<UserEntity>> response = userController.postUsersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userList, response.getBody());
        verify(userService, times(1)).searchUsers(any());
    }

}
