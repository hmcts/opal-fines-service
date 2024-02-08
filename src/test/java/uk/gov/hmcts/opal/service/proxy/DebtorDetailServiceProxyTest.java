package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDebtorDetailService;
import uk.gov.hmcts.opal.service.opal.DebtorDetailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DebtorDetailServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private DebtorDetailService opalDebtorDetailService;

    @Mock
    private LegacyDebtorDetailService legacyDebtorDetailService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalDebtorDetailServiceWhenModeIsNotLegacy() {
        // Given: a DebtorDetailEntity and the app mode is set to "opal"
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalDebtorDetailService.getDebtorDetail(anyLong())).thenReturn(entity);

        // When: saveDebtorDetail is called on the proxy
        DebtorDetailEntity debtorDetailResult = debtorDetailServiceProxy.getDebtorDetail(1);

        // Then: opalDebtorDetailService should be used, and the returned debtorDetail should be as expected
        verify(opalDebtorDetailService).getDebtorDetail(1);
        verifyNoInteractions(legacyDebtorDetailService);
        Assertions.assertEquals(entity, debtorDetailResult);

        // Given: a debtorDetails list result and the app mode is set to "opal"
        List<DebtorDetailEntity> debtorDetailsList = List.of(entity);
        when(opalDebtorDetailService.searchDebtorDetails(any())).thenReturn(debtorDetailsList);

        // When: searchDebtorDetails is called on the proxy
        DebtorDetailSearchDto criteria = DebtorDetailSearchDto.builder().build();
        List<DebtorDetailEntity> listResult = debtorDetailServiceProxy.searchDebtorDetails(criteria);

        // Then: opalDebtorDetailService should be used, and the returned list should be as expected
        verify(opalDebtorDetailService).searchDebtorDetails(criteria);
        verifyNoInteractions(legacyDebtorDetailService);
        Assertions.assertEquals(debtorDetailsList, listResult);
    }

    @Test
    void shouldUseLegacyDebtorDetailServiceWhenModeIsLegacy() {
        // Given: a DebtorDetailEntity and the app mode is set to "legacy"
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyDebtorDetailService.getDebtorDetail(anyLong())).thenReturn(entity);

        // When: saveDebtorDetail is called on the proxy
        DebtorDetailEntity result = debtorDetailServiceProxy.getDebtorDetail(1);

        // Then: legacyDebtorDetailService should be used, and the returned debtorDetail should be as expected
        verify(legacyDebtorDetailService).getDebtorDetail(1);
        verifyNoInteractions(opalDebtorDetailService);
        Assertions.assertEquals(entity, result);

        // Given: a debtorDetails list result and the app mode is set to "legacy"
        List<DebtorDetailEntity> debtorDetailsList = List.of(entity);
        when(legacyDebtorDetailService.searchDebtorDetails(any())).thenReturn(debtorDetailsList);

        // When: searchDebtorDetails is called on the proxy
        DebtorDetailSearchDto criteria = DebtorDetailSearchDto.builder().build();
        List<DebtorDetailEntity> listResult = debtorDetailServiceProxy.searchDebtorDetails(criteria);

        // Then: opalDebtorDetailService should be used, and the returned list should be as expected
        verify(legacyDebtorDetailService).searchDebtorDetails(criteria);
        verifyNoInteractions(opalDebtorDetailService);
        Assertions.assertEquals(debtorDetailsList, listResult); // Not yet implemented in Legacy mode
    }
}
