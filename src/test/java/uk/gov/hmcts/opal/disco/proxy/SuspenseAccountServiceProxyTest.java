package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.disco.SuspenseAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacySuspenseAccountService;
import uk.gov.hmcts.opal.disco.opal.SuspenseAccountService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SuspenseAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private SuspenseAccountService opalService;

    @Mock
    private LegacySuspenseAccountService legacyService;

    @InjectMocks
    private SuspenseAccountServiceProxy suspenseAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(SuspenseAccountServiceInterface targetService, SuspenseAccountServiceInterface otherService) {
        testGetSuspenseAccount(targetService, otherService);
        testSearchSuspenseAccounts(targetService, otherService);
    }

    void testGetSuspenseAccount(SuspenseAccountServiceInterface targetService,
                                SuspenseAccountServiceInterface otherService) {
        // Given: a SuspenseAccountEntity is returned from the target service
        SuspenseAccountEntity entity = SuspenseAccountEntity.builder().build();
        when(targetService.getSuspenseAccount(anyLong())).thenReturn(entity);

        // When: getSuspenseAccount is called on the proxy
        SuspenseAccountEntity suspenseAccountResult = suspenseAccountServiceProxy.getSuspenseAccount(1);

        // Then: target service should be used, and the returned suspenseAccount should be as expected
        verify(targetService).getSuspenseAccount(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, suspenseAccountResult);
    }

    void testSearchSuspenseAccounts(SuspenseAccountServiceInterface targetService,
                                    SuspenseAccountServiceInterface otherService) {
        // Given: a suspenseAccounts list result is returned from the target service
        SuspenseAccountEntity entity = SuspenseAccountEntity.builder().build();
        List<SuspenseAccountEntity> suspenseAccountsList = List.of(entity);
        when(targetService.searchSuspenseAccounts(any())).thenReturn(suspenseAccountsList);

        // When: searchSuspenseAccounts is called on the proxy
        SuspenseAccountSearchDto criteria = SuspenseAccountSearchDto.builder().build();
        List<SuspenseAccountEntity> listResult = suspenseAccountServiceProxy.searchSuspenseAccounts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchSuspenseAccounts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(suspenseAccountsList, listResult);
    }

    @Test
    void shouldUseOpalSuspenseAccountServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacySuspenseAccountServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
