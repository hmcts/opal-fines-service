package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.UserEntitlementController;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.service.opal.UserEntitlementService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEntitlementControllerTest {

    @Mock
    private UserEntitlementService userEntitlementService;

    @InjectMocks
    private UserEntitlementController userEntitlementController;

    @Test
    void testGetUserEntitlement_Success() {
        // Arrange
        UserEntitlementEntity entity = UserEntitlementEntity.builder().build();

        when(userEntitlementService.getUserEntitlement(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<UserEntitlementEntity> response = userEntitlementController.getUserEntitlementById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(userEntitlementService, times(1)).getUserEntitlement(any(Long.class));
    }

    @Test
    void testSearchUserEntitlements_Success() {
        // Arrange
        UserEntitlementEntity entity = UserEntitlementEntity.builder().build();
        List<UserEntitlementEntity> userEntitlementList = List.of(entity);

        when(userEntitlementService.searchUserEntitlements(any())).thenReturn(userEntitlementList);

        // Act
        UserEntitlementSearchDto searchDto = UserEntitlementSearchDto.builder().build();
        ResponseEntity<List<UserEntitlementEntity>> response = userEntitlementController
            .postUserEntitlementsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userEntitlementList, response.getBody());
        verify(userEntitlementService, times(1)).searchUserEntitlements(any());
    }

}
