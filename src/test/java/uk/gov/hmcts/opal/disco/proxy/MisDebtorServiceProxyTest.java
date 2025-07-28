package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.disco.MisDebtorServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyMisDebtorService;
import uk.gov.hmcts.opal.disco.opal.MisDebtorService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MisDebtorServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private MisDebtorService opalService;

    @Mock
    private LegacyMisDebtorService legacyService;

    @InjectMocks
    private MisDebtorServiceProxy misDebtorServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(MisDebtorServiceInterface targetService, MisDebtorServiceInterface otherService) {
        testGetMisDebtor(targetService, otherService);
        testSearchMisDebtors(targetService, otherService);
    }

    void testGetMisDebtor(MisDebtorServiceInterface targetService, MisDebtorServiceInterface otherService) {
        // Given: a MisDebtorEntity is returned from the target service
        MisDebtorEntity entity = MisDebtorEntity.builder().build();
        when(targetService.getMisDebtor(anyLong())).thenReturn(entity);

        // When: getMisDebtor is called on the proxy
        MisDebtorEntity misDebtorResult = misDebtorServiceProxy.getMisDebtor(1);

        // Then: target service should be used, and the returned misDebtor should be as expected
        verify(targetService).getMisDebtor(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, misDebtorResult);
    }

    void testSearchMisDebtors(MisDebtorServiceInterface targetService, MisDebtorServiceInterface otherService) {
        // Given: a misDebtors list result is returned from the target service
        MisDebtorEntity entity = MisDebtorEntity.builder().build();
        List<MisDebtorEntity> misDebtorsList = List.of(entity);
        when(targetService.searchMisDebtors(any())).thenReturn(misDebtorsList);

        // When: searchMisDebtors is called on the proxy
        MisDebtorSearchDto criteria = MisDebtorSearchDto.builder().build();
        List<MisDebtorEntity> listResult = misDebtorServiceProxy.searchMisDebtors(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchMisDebtors(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(misDebtorsList, listResult);
    }

    @Test
    void shouldUseOpalMisDebtorServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyMisDebtorServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
