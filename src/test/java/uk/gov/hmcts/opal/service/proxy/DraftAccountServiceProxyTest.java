package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.DraftAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountService;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DraftAccountServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DraftAccountService opalService;

    @Mock
    private LegacyDraftAccountService legacyService;

    @InjectMocks
    private DraftAccountServiceProxy draftAccountServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DraftAccountServiceInterface targetService, DraftAccountServiceInterface otherService) {
        testGetDraftAccount(targetService, otherService);
        testSearchDraftAccounts(targetService, otherService);
    }

    void testGetDraftAccount(DraftAccountServiceInterface targetService, DraftAccountServiceInterface otherService) {
        // Given: a DraftAccountEntity is returned from the target service
        DraftAccountEntity entity = DraftAccountEntity.builder().build();
        when(targetService.getDraftAccount(anyLong())).thenReturn(entity);

        // When: getDraftAccount is called on the proxy
        DraftAccountEntity draftAccountResult = draftAccountServiceProxy.getDraftAccount(1);

        // Then: target service should be used, and the returned draftAccount should be as expected
        verify(targetService).getDraftAccount(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, draftAccountResult);
    }

    void testSearchDraftAccounts(DraftAccountServiceInterface targetService,
                                 DraftAccountServiceInterface otherService) {
        // Given: a draftAccounts list result is returned from the target service
        DraftAccountEntity entity = DraftAccountEntity.builder().build();
        List<DraftAccountEntity> draftAccountsList = List.of(entity);
        when(targetService.searchDraftAccounts(any())).thenReturn(draftAccountsList);

        // When: searchDraftAccounts is called on the proxy
        DraftAccountSearchDto criteria = DraftAccountSearchDto.builder().build();
        List<DraftAccountEntity> listResult = draftAccountServiceProxy.searchDraftAccounts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDraftAccounts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(draftAccountsList, listResult);
    }

    @Test
    void shouldUseOpalDraftAccountServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDraftAccountServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
