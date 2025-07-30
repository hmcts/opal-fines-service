package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.disco.opal.AccountTransferService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransferControllerTest {

    @Mock
    private AccountTransferService accountTransferService;

    @InjectMocks
    private AccountTransferController accountTransferController;

    @Test
    void testGetAccountTransfer_Success() {
        // Arrange
        AccountTransferEntity entity = AccountTransferEntity.builder().build();

        when(accountTransferService.getAccountTransfer(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<AccountTransferEntity> response = accountTransferController.getAccountTransferById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(accountTransferService, times(1)).getAccountTransfer(any(Long.class));
    }

    @Test
    void testSearchAccountTransfers_Success() {
        // Arrange
        AccountTransferEntity entity = AccountTransferEntity.builder().build();
        List<AccountTransferEntity> accountTransferList = List.of(entity);

        when(accountTransferService.searchAccountTransfers(any())).thenReturn(accountTransferList);

        // Act
        AccountTransferSearchDto searchDto = AccountTransferSearchDto.builder().build();
        ResponseEntity<List<AccountTransferEntity>> response = accountTransferController
            .postAccountTransfersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountTransferList, response.getBody());
        verify(accountTransferService, times(1)).searchAccountTransfers(any());
    }

}
