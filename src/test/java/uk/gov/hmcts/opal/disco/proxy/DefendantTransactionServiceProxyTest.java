package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.disco.DefendantTransactionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyDefendantTransactionService;
import uk.gov.hmcts.opal.disco.opal.DefendantTransactionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefendantTransactionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DefendantTransactionService opalService;

    @Mock
    private LegacyDefendantTransactionService legacyService;

    @InjectMocks
    private DefendantTransactionServiceProxy defendantTransactionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DefendantTransactionServiceInterface targetService,
                  DefendantTransactionServiceInterface otherService) {
        testGetDefendantTransaction(targetService, otherService);
        testSearchDefendantTransactions(targetService, otherService);
    }

    void testGetDefendantTransaction(DefendantTransactionServiceInterface targetService,
                                     DefendantTransactionServiceInterface otherService) {
        // Given: a DefendantTransactionEntity is returned from the target service
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder().build();
        when(targetService.getDefendantTransaction(anyLong())).thenReturn(entity);

        // When: getDefendantTransaction is called on the proxy
        DefendantTransactionEntity defendantTransactionResult = defendantTransactionServiceProxy
            .getDefendantTransaction(1);

        // Then: target service should be used, and the returned defendantTransaction should be as expected
        verify(targetService).getDefendantTransaction(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, defendantTransactionResult);
    }

    void testSearchDefendantTransactions(DefendantTransactionServiceInterface targetService,
                                         DefendantTransactionServiceInterface otherService) {
        // Given: a defendantTransactions list result is returned from the target service
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder().build();
        List<DefendantTransactionEntity> defendantTransactionsList = List.of(entity);
        when(targetService.searchDefendantTransactions(any())).thenReturn(defendantTransactionsList);

        // When: searchDefendantTransactions is called on the proxy
        DefendantTransactionSearchDto criteria = DefendantTransactionSearchDto.builder().build();
        List<DefendantTransactionEntity> listResult = defendantTransactionServiceProxy
            .searchDefendantTransactions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDefendantTransactions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(defendantTransactionsList, listResult);
    }

    @Test
    void shouldUseOpalDefendantTransactionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDefendantTransactionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
