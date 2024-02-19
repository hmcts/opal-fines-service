package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.DebtorDetailServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDebtorDetailService;
import uk.gov.hmcts.opal.service.opal.DebtorDetailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DebtorDetailServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DebtorDetailService opalService;

    @Mock
    private LegacyDebtorDetailService legacyService;

    @InjectMocks
    private DebtorDetailServiceProxy debtorDetailServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DebtorDetailServiceInterface targetService, DebtorDetailServiceInterface otherService) {
        testGetDebtorDetail(targetService, otherService);
        testSearchDebtorDetails(targetService, otherService);
    }

    void testGetDebtorDetail(DebtorDetailServiceInterface targetService, DebtorDetailServiceInterface otherService) {
        // Given: a DebtorDetailEntity is returned from the target service
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build();
        when(targetService.getDebtorDetail(anyLong())).thenReturn(entity);

        // When: getDebtorDetail is called on the proxy
        DebtorDetailEntity debtorDetailResult = debtorDetailServiceProxy.getDebtorDetail(1);

        // Then: target service should be used, and the returned debtorDetail should be as expected
        verify(targetService).getDebtorDetail(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, debtorDetailResult);
    }

    void testSearchDebtorDetails(DebtorDetailServiceInterface targetService,
                                 DebtorDetailServiceInterface otherService) {
        // Given: a debtorDetails list result is returned from the target service
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build();
        List<DebtorDetailEntity> debtorDetailsList = List.of(entity);
        when(targetService.searchDebtorDetails(any())).thenReturn(debtorDetailsList);

        // When: searchDebtorDetails is called on the proxy
        DebtorDetailSearchDto criteria = DebtorDetailSearchDto.builder().build();
        List<DebtorDetailEntity> listResult = debtorDetailServiceProxy.searchDebtorDetails(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDebtorDetails(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(debtorDetailsList, listResult);
    }

    @Test
    void shouldUseOpalDebtorDetailServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDebtorDetailServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
