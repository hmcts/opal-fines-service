package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.service.opal.DefendantTransactionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantTransactionControllerTest {

    @Mock
    private DefendantTransactionService defendantTransactionService;

    @InjectMocks
    private DefendantTransactionController defendantTransactionController;

    @Test
    void testGetDefendantTransaction_Success() {
        // Arrange
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder().build();

        when(defendantTransactionService.getDefendantTransaction(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DefendantTransactionEntity> response = defendantTransactionController
            .getDefendantTransactionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(defendantTransactionService, times(1)).getDefendantTransaction(any(Long.class));
    }

    @Test
    void testSearchDefendantTransactions_Success() {
        // Arrange
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder().build();
        List<DefendantTransactionEntity> defendantTransactionList = List.of(entity);

        when(defendantTransactionService.searchDefendantTransactions(any())).thenReturn(defendantTransactionList);

        // Act
        DefendantTransactionSearchDto searchDto = DefendantTransactionSearchDto.builder().build();
        ResponseEntity<List<DefendantTransactionEntity>> response = defendantTransactionController
            .postDefendantTransactionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(defendantTransactionList, response.getBody());
        verify(defendantTransactionService, times(1)).searchDefendantTransactions(any());
    }

}
