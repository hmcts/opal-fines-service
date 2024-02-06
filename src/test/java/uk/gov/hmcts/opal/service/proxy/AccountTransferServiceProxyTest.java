package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyAccountTransferService;
import uk.gov.hmcts.opal.service.opal.AccountTransferService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AccountTransferServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private AccountTransferService opalAccountTransferService;

    @Mock
    private LegacyAccountTransferService legacyAccountTransferService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalAccountTransferServiceWhenModeIsNotLegacy() {
        // Given: a AccountTransferEntity and the app mode is set to "opal"
        AccountTransferEntity entity = AccountTransferEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalAccountTransferService.getAccountTransfer(anyLong())).thenReturn(entity);

        // When: saveAccountTransfer is called on the proxy
        AccountTransferEntity accountTransferResult = accountTransferServiceProxy.getAccountTransfer(1);

        // Then: opalAccountTransferService should be used, and the returned accountTransfer should be as expected
        verify(opalAccountTransferService).getAccountTransfer(1);
        verifyNoInteractions(legacyAccountTransferService);
        Assertions.assertEquals(entity, accountTransferResult);

        // Given: a accountTransfers list result and the app mode is set to "opal"
        List<AccountTransferEntity> accountTransfersList = List.of(entity);
        when(opalAccountTransferService.searchAccountTransfers(any())).thenReturn(accountTransfersList);

        // When: searchAccountTransfers is called on the proxy
        AccountTransferSearchDto criteria = AccountTransferSearchDto.builder().build();
        List<AccountTransferEntity> listResult = accountTransferServiceProxy.searchAccountTransfers(criteria);

        // Then: opalAccountTransferService should be used, and the returned list should be as expected
        verify(opalAccountTransferService).searchAccountTransfers(criteria);
        verifyNoInteractions(legacyAccountTransferService);
        Assertions.assertEquals(accountTransfersList, listResult);
    }

    @Test
    void shouldUseLegacyAccountTransferServiceWhenModeIsLegacy() {
        // Given: a AccountTransferEntity and the app mode is set to "legacy"
        AccountTransferEntity entity = AccountTransferEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyAccountTransferService.getAccountTransfer(anyLong())).thenReturn(entity);

        // When: saveAccountTransfer is called on the proxy
        AccountTransferEntity result = accountTransferServiceProxy.getAccountTransfer(1);

        // Then: legacyAccountTransferService should be used, and the returned accountTransfer should be as expected
        verify(legacyAccountTransferService).getAccountTransfer(1);
        verifyNoInteractions(opalAccountTransferService);
        Assertions.assertEquals(entity, result);

        // Given: a accountTransfers list result and the app mode is set to "legacy"
        List<AccountTransferEntity> accountTransfersList = List.of(entity);
        when(legacyAccountTransferService.searchAccountTransfers(any())).thenReturn(accountTransfersList);

        // When: searchAccountTransfers is called on the proxy
        AccountTransferSearchDto criteria = AccountTransferSearchDto.builder().build();
        List<AccountTransferEntity> listResult = accountTransferServiceProxy.searchAccountTransfers(criteria);

        // Then: opalAccountTransferService should be used, and the returned list should be as expected
        verify(legacyAccountTransferService).searchAccountTransfers(criteria);
        verifyNoInteractions(opalAccountTransferService);
        Assertions.assertEquals(accountTransfersList, listResult); // Not yet implemented in Legacy mode
    }
}
