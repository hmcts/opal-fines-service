package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.service.ChequeServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyChequeService;
import uk.gov.hmcts.opal.service.opal.ChequeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChequeServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ChequeService opalService;

    @Mock
    private LegacyChequeService legacyService;

    @InjectMocks
    private ChequeServiceProxy chequeServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ChequeServiceInterface targetService, ChequeServiceInterface otherService) {
        testGetCheque(targetService, otherService);
        testSearchCheques(targetService, otherService);
    }

    void testGetCheque(ChequeServiceInterface targetService, ChequeServiceInterface otherService) {
        // Given: a ChequeEntity is returned from the target service
        ChequeEntity entity = ChequeEntity.builder().build();
        when(targetService.getCheque(anyLong())).thenReturn(entity);

        // When: getCheque is called on the proxy
        ChequeEntity chequeResult = chequeServiceProxy.getCheque(1);

        // Then: target service should be used, and the returned cheque should be as expected
        verify(targetService).getCheque(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, chequeResult);
    }

    void testSearchCheques(ChequeServiceInterface targetService, ChequeServiceInterface otherService) {
        // Given: a cheques list result is returned from the target service
        ChequeEntity entity = ChequeEntity.builder().build();
        List<ChequeEntity> chequesList = List.of(entity);
        when(targetService.searchCheques(any())).thenReturn(chequesList);

        // When: searchCheques is called on the proxy
        ChequeSearchDto criteria = ChequeSearchDto.builder().build();
        List<ChequeEntity> listResult = chequeServiceProxy.searchCheques(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCheques(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(chequesList, listResult);
    }

    @Test
    void shouldUseOpalChequeServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyChequeServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
