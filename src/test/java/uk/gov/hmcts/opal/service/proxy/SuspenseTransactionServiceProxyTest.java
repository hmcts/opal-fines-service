package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseTransactionService;
import uk.gov.hmcts.opal.service.opal.SuspenseTransactionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SuspenseTransactionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private SuspenseTransactionService opalService;

    @Mock
    private LegacySuspenseTransactionService legacyService;

    @InjectMocks
    private SuspenseTransactionServiceProxy suspenseTransactionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(SuspenseTransactionServiceInterface targetService, SuspenseTransactionServiceInterface otherService) {
        testGetSuspenseTransaction(targetService, otherService);
        testSearchSuspenseTransactions(targetService, otherService);
    }

    void testGetSuspenseTransaction(SuspenseTransactionServiceInterface targetService,
                                    SuspenseTransactionServiceInterface otherService) {
        // Given: a SuspenseTransactionEntity is returned from the target service
        SuspenseTransactionEntity entity = SuspenseTransactionEntity.builder().build();
        when(targetService.getSuspenseTransaction(anyLong())).thenReturn(entity);

        // When: getSuspenseTransaction is called on the proxy
        SuspenseTransactionEntity suspenseTransactionResult = suspenseTransactionServiceProxy
            .getSuspenseTransaction(1);

        // Then: target service should be used, and the returned suspenseTransaction should be as expected
        verify(targetService).getSuspenseTransaction(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, suspenseTransactionResult);
    }

    void testSearchSuspenseTransactions(SuspenseTransactionServiceInterface targetService,
                                        SuspenseTransactionServiceInterface otherService) {
        // Given: a suspenseTransactions list result is returned from the target service
        SuspenseTransactionEntity entity = SuspenseTransactionEntity.builder().build();
        List<SuspenseTransactionEntity> suspenseTransactionsList = List.of(entity);
        when(targetService.searchSuspenseTransactions(any())).thenReturn(suspenseTransactionsList);

        // When: searchSuspenseTransactions is called on the proxy
        SuspenseTransactionSearchDto criteria = SuspenseTransactionSearchDto.builder().build();
        List<SuspenseTransactionEntity> listResult = suspenseTransactionServiceProxy
            .searchSuspenseTransactions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchSuspenseTransactions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(suspenseTransactionsList, listResult);
    }

    @Test
    void shouldUseOpalSuspenseTransactionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacySuspenseTransactionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
