package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.disco.AccountTransferServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyAccountTransferService;
import uk.gov.hmcts.opal.disco.opal.AccountTransferService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountTransferServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private AccountTransferService opalService;

    @Mock
    private LegacyAccountTransferService legacyService;

    @InjectMocks
    private AccountTransferServiceProxy accountTransferServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(AccountTransferServiceInterface targetService, AccountTransferServiceInterface otherService) {
        testGetAccountTransfer(targetService, otherService);
        testSearchAccountTransfers(targetService, otherService);
    }

    void testGetAccountTransfer(AccountTransferServiceInterface targetService,
                                AccountTransferServiceInterface otherService) {
        // Given: an AccountTransferEntity is returned from the target service
        AccountTransferEntity entity = AccountTransferEntity.builder().build();
        when(targetService.getAccountTransfer(anyLong())).thenReturn(entity);

        // When: getAccountTransfer is called on the proxy
        AccountTransferEntity accountTransferResult = accountTransferServiceProxy.getAccountTransfer(1);

        // Then: target service should be used, and the returned accountTransfer should be as expected
        verify(targetService).getAccountTransfer(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, accountTransferResult);
    }

    void testSearchAccountTransfers(AccountTransferServiceInterface targetService,
                                    AccountTransferServiceInterface otherService) {
        // Given: an accountTransfers list result is returned from the target service
        AccountTransferEntity entity = AccountTransferEntity.builder().build();
        List<AccountTransferEntity> accountTransfersList = List.of(entity);
        when(targetService.searchAccountTransfers(any())).thenReturn(accountTransfersList);

        // When: searchAccountTransfers is called on the proxy
        AccountTransferSearchDto criteria = AccountTransferSearchDto.builder().build();
        List<AccountTransferEntity> listResult = accountTransferServiceProxy.searchAccountTransfers(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchAccountTransfers(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(accountTransfersList, listResult);
    }

    @Test
    void shouldUseOpalAccountTransferServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyAccountTransferServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
