package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantTransactionServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyDefendantTransactionService legacyDefendantTransactionService;

    @Test
    void testGetDefendantTransaction() {
        // Arrange

        DefendantTransactionEntity defendantTransactionEntity = DefendantTransactionEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantTransactionService.getDefendantTransaction(1)
        );

        // Assert
        assertNotNull(legacyDefendantTransactionService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchDefendantTransactions() {
        // Arrange
        DefendantTransactionSearchDto criteria = DefendantTransactionSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDefendantTransactionService.searchDefendantTransactions(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
