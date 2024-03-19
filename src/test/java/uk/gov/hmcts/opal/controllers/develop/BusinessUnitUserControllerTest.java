package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.BusinessUnitUserController;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.service.opal.BusinessUnitUserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitUserControllerTest {

    @Mock
    private BusinessUnitUserService businessUnitUserService;

    @InjectMocks
    private BusinessUnitUserController businessUnitUserController;

    @Test
    void testGetBusinessUnitUser_Success() {
        // Arrange
        BusinessUnitUserEntity entity = BusinessUnitUserEntity.builder().build();

        when(businessUnitUserService.getBusinessUnitUser(anyString())).thenReturn(entity);

        // Act
        ResponseEntity<BusinessUnitUserEntity> response = businessUnitUserController.getBusinessUnitUserById("1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(businessUnitUserService, times(1)).getBusinessUnitUser(anyString());
    }

    @Test
    void testSearchBusinessUnitUsers_Success() {
        // Arrange
        BusinessUnitUserEntity entity = BusinessUnitUserEntity.builder().build();
        List<BusinessUnitUserEntity> businessUnitUserList = List.of(entity);

        when(businessUnitUserService.searchBusinessUnitUsers(any())).thenReturn(businessUnitUserList);

        // Act
        BusinessUnitUserSearchDto searchDto = BusinessUnitUserSearchDto.builder().build();
        ResponseEntity<List<BusinessUnitUserEntity>> response = businessUnitUserController
            .postBusinessUnitUsersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(businessUnitUserList, response.getBody());
        verify(businessUnitUserService, times(1)).searchBusinessUnitUsers(any());
    }

}
