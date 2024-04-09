package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.service.CreditorTransactionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyCreditorTransactionService;
import uk.gov.hmcts.opal.service.opal.CreditorTransactionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreditorTransactionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private CreditorTransactionService opalService;

    @Mock
    private LegacyCreditorTransactionService legacyService;

    @InjectMocks
    private CreditorTransactionServiceProxy creditorTransactionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(CreditorTransactionServiceInterface targetService, CreditorTransactionServiceInterface otherService) {
        testGetCreditorTransaction(targetService, otherService);
        testSearchCreditorTransactions(targetService, otherService);
    }

    void testGetCreditorTransaction(CreditorTransactionServiceInterface targetService,
                                    CreditorTransactionServiceInterface otherService) {
        // Given: a CreditorTransactionEntity is returned from the target service
        CreditorTransactionEntity entity = CreditorTransactionEntity.builder().build();
        when(targetService.getCreditorTransaction(anyLong())).thenReturn(entity);

        // When: getCreditorTransaction is called on the proxy
        CreditorTransactionEntity creditorTransactionResult = creditorTransactionServiceProxy
            .getCreditorTransaction(1);

        // Then: target service should be used, and the returned creditorTransaction should be as expected
        verify(targetService).getCreditorTransaction(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, creditorTransactionResult);
    }

    void testSearchCreditorTransactions(CreditorTransactionServiceInterface targetService,
                                        CreditorTransactionServiceInterface otherService) {
        // Given: a creditorTransactions list result is returned from the target service
        CreditorTransactionEntity entity = CreditorTransactionEntity.builder().build();
        List<CreditorTransactionEntity> creditorTransactionsList = List.of(entity);
        when(targetService.searchCreditorTransactions(any())).thenReturn(creditorTransactionsList);

        // When: searchCreditorTransactions is called on the proxy
        CreditorTransactionSearchDto criteria = CreditorTransactionSearchDto.builder().build();
        List<CreditorTransactionEntity> listResult = creditorTransactionServiceProxy
            .searchCreditorTransactions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCreditorTransactions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(creditorTransactionsList, listResult);
    }

    @Test
    void shouldUseOpalCreditorTransactionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyCreditorTransactionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
