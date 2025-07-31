package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.disco.opal.CreditorTransactionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditorTransactionControllerTest {

    @Mock
    private CreditorTransactionService creditorTransactionService;

    @InjectMocks
    private CreditorTransactionController creditorTransactionController;

    @Test
    void testGetCreditorTransaction_Success() {
        // Arrange
        CreditorTransactionEntity entity = CreditorTransactionEntity.builder().build();

        when(creditorTransactionService.getCreditorTransaction(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CreditorTransactionEntity> response = creditorTransactionController
            .getCreditorTransactionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(creditorTransactionService, times(1)).getCreditorTransaction(any(Long.class));
    }

    @Test
    void testSearchCreditorTransactions_Success() {
        // Arrange
        CreditorTransactionEntity entity = CreditorTransactionEntity.builder().build();
        List<CreditorTransactionEntity> creditorTransactionList = List.of(entity);

        when(creditorTransactionService.searchCreditorTransactions(any())).thenReturn(creditorTransactionList);

        // Act
        CreditorTransactionSearchDto searchDto = CreditorTransactionSearchDto.builder().build();
        ResponseEntity<List<CreditorTransactionEntity>> response = creditorTransactionController
            .postCreditorTransactionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(creditorTransactionList, response.getBody());
        verify(creditorTransactionService, times(1)).searchCreditorTransactions(any());
    }

}
