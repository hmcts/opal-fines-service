package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMiscellaneousAccountService;
import uk.gov.hmcts.opal.service.opal.MiscellaneousAccountService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MiscellaneousAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private MiscellaneousAccountService opalService;

    @Mock
    private LegacyMiscellaneousAccountService legacyService;

    @InjectMocks
    private MiscellaneousAccountServiceProxy miscellaneousAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(MiscellaneousAccountServiceInterface targetService,
                  MiscellaneousAccountServiceInterface otherService) {
        testGetMiscellaneousAccount(targetService, otherService);
        testSearchMiscellaneousAccounts(targetService, otherService);
    }

    void testGetMiscellaneousAccount(MiscellaneousAccountServiceInterface targetService,
                                     MiscellaneousAccountServiceInterface otherService) {
        // Given: a MiscellaneousAccountEntity is returned from the target service
        MiscellaneousAccountEntity entity = MiscellaneousAccountEntity.builder().build();
        when(targetService.getMiscellaneousAccount(anyLong())).thenReturn(entity);

        // When: getMiscellaneousAccount is called on the proxy
        MiscellaneousAccountEntity miscellaneousAccountResult = miscellaneousAccountServiceProxy
            .getMiscellaneousAccount(1);

        // Then: target service should be used, and the returned miscellaneousAccount should be as expected
        verify(targetService).getMiscellaneousAccount(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, miscellaneousAccountResult);
    }

    void testSearchMiscellaneousAccounts(MiscellaneousAccountServiceInterface targetService,
                                         MiscellaneousAccountServiceInterface otherService) {
        // Given: a miscellaneousAccounts list result is returned from the target service
        MiscellaneousAccountEntity entity = MiscellaneousAccountEntity.builder().build();
        List<MiscellaneousAccountEntity> miscellaneousAccountsList = List.of(entity);
        when(targetService.searchMiscellaneousAccounts(any())).thenReturn(miscellaneousAccountsList);

        // When: searchMiscellaneousAccounts is called on the proxy
        MiscellaneousAccountSearchDto criteria = MiscellaneousAccountSearchDto.builder().build();
        List<MiscellaneousAccountEntity> listResult = miscellaneousAccountServiceProxy
            .searchMiscellaneousAccounts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchMiscellaneousAccounts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(miscellaneousAccountsList, listResult);
    }

    @Test
    void shouldUseOpalMiscellaneousAccountServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyMiscellaneousAccountServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
