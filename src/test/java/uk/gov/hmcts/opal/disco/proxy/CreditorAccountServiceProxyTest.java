package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.disco.CreditorAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyCreditorAccountService;
import uk.gov.hmcts.opal.disco.opal.CreditorAccountService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreditorAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private CreditorAccountService opalService;

    @Mock
    private LegacyCreditorAccountService legacyService;

    @InjectMocks
    private CreditorAccountServiceProxy creditorAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(CreditorAccountServiceInterface targetService, CreditorAccountServiceInterface otherService) {
        testGetCreditorAccount(targetService, otherService);
        testSearchCreditorAccounts(targetService, otherService);
    }

    void testGetCreditorAccount(CreditorAccountServiceInterface targetService,
                                CreditorAccountServiceInterface otherService) {
        // Given: a CreditorAccountEntity is returned from the target service
        CreditorAccountEntity entity = CreditorAccountEntity.builder().build();
        when(targetService.getCreditorAccount(anyLong())).thenReturn(entity);

        // When: getCreditorAccount is called on the proxy
        CreditorAccountEntity creditorAccountResult = creditorAccountServiceProxy.getCreditorAccount(1);

        // Then: target service should be used, and the returned creditorAccount should be as expected
        verify(targetService).getCreditorAccount(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, creditorAccountResult);
    }

    void testSearchCreditorAccounts(CreditorAccountServiceInterface targetService,
                                    CreditorAccountServiceInterface otherService) {
        // Given: a creditorAccounts list result is returned from the target service
        CreditorAccountEntity entity = CreditorAccountEntity.builder().build();
        List<CreditorAccountEntity> creditorAccountsList = List.of(entity);
        when(targetService.searchCreditorAccounts(any())).thenReturn(creditorAccountsList);

        // When: searchCreditorAccounts is called on the proxy
        CreditorAccountSearchDto criteria = CreditorAccountSearchDto.builder().build();
        List<CreditorAccountEntity> listResult = creditorAccountServiceProxy.searchCreditorAccounts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCreditorAccounts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(creditorAccountsList, listResult);
    }

    @Test
    void shouldUseOpalCreditorAccountServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyCreditorAccountServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
