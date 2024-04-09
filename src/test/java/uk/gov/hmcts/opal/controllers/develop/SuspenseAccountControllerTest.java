package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.service.opal.SuspenseAccountService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountControllerTest {

    @Mock
    private SuspenseAccountService suspenseAccountService;

    @InjectMocks
    private SuspenseAccountController suspenseAccountController;

    @Test
    void testGetSuspenseAccount_Success() {
        // Arrange
        SuspenseAccountEntity entity = SuspenseAccountEntity.builder().build();

        when(suspenseAccountService.getSuspenseAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<SuspenseAccountEntity> response = suspenseAccountController.getSuspenseAccountById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(suspenseAccountService, times(1)).getSuspenseAccount(any(Long.class));
    }

    @Test
    void testSearchSuspenseAccounts_Success() {
        // Arrange
        SuspenseAccountEntity entity = SuspenseAccountEntity.builder().build();
        List<SuspenseAccountEntity> suspenseAccountList = List.of(entity);

        when(suspenseAccountService.searchSuspenseAccounts(any())).thenReturn(suspenseAccountList);

        // Act
        SuspenseAccountSearchDto searchDto = SuspenseAccountSearchDto.builder().build();
        ResponseEntity<List<SuspenseAccountEntity>> response = suspenseAccountController
            .postSuspenseAccountsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(suspenseAccountList, response.getBody());
        verify(suspenseAccountService, times(1)).searchSuspenseAccounts(any());
    }

}
