package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.disco.opal.SuspenseTransactionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseTransactionControllerTest {

    @Mock
    private SuspenseTransactionService suspenseTransactionService;

    @InjectMocks
    private SuspenseTransactionController suspenseTransactionController;

    @Test
    void testGetSuspenseTransaction_Success() {
        // Arrange
        SuspenseTransactionEntity entity = SuspenseTransactionEntity.builder().build();

        when(suspenseTransactionService.getSuspenseTransaction(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<SuspenseTransactionEntity> response = suspenseTransactionController
            .getSuspenseTransactionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(suspenseTransactionService, times(1)).getSuspenseTransaction(any(Long.class));
    }

    @Test
    void testSearchSuspenseTransactions_Success() {
        // Arrange
        SuspenseTransactionEntity entity = SuspenseTransactionEntity.builder().build();
        List<SuspenseTransactionEntity> suspenseTransactionList = List.of(entity);

        when(suspenseTransactionService.searchSuspenseTransactions(any())).thenReturn(suspenseTransactionList);

        // Act
        SuspenseTransactionSearchDto searchDto = SuspenseTransactionSearchDto.builder().build();
        ResponseEntity<List<SuspenseTransactionEntity>> response = suspenseTransactionController
            .postSuspenseTransactionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(suspenseTransactionList, response.getBody());
        verify(suspenseTransactionService, times(1)).searchSuspenseTransactions(any());
    }

}
