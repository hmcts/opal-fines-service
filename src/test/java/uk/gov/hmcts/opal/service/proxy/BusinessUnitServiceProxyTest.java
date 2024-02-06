package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyBusinessUnitService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BusinessUnitServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private BusinessUnitService opalBusinessUnitService;

    @Mock
    private LegacyBusinessUnitService legacyBusinessUnitService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private BusinessUnitServiceProxy businessUnitServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalBusinessUnitServiceWhenModeIsNotLegacy() {
        // Given: a BusinessUnitEntity and the app mode is set to "opal"
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalBusinessUnitService.getBusinessUnit(anyLong())).thenReturn(entity);

        // When: saveBusinessUnit is called on the proxy
        BusinessUnitEntity businessUnitResult = businessUnitServiceProxy.getBusinessUnit(1);

        // Then: opalBusinessUnitService should be used, and the returned businessUnit should be as expected
        verify(opalBusinessUnitService).getBusinessUnit(1);
        verifyNoInteractions(legacyBusinessUnitService);
        Assertions.assertEquals(entity, businessUnitResult);

        // Given: a businessUnits list result and the app mode is set to "opal"
        List<BusinessUnitEntity> businessUnitsList = List.of(entity);
        when(opalBusinessUnitService.searchBusinessUnits(any())).thenReturn(businessUnitsList);

        // When: searchBusinessUnits is called on the proxy
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        List<BusinessUnitEntity> listResult = businessUnitServiceProxy.searchBusinessUnits(criteria);

        // Then: opalBusinessUnitService should be used, and the returned list should be as expected
        verify(opalBusinessUnitService).searchBusinessUnits(criteria);
        verifyNoInteractions(legacyBusinessUnitService);
        Assertions.assertEquals(businessUnitsList, listResult);
    }

    @Test
    void shouldUseLegacyBusinessUnitServiceWhenModeIsLegacy() {
        // Given: a BusinessUnitEntity and the app mode is set to "legacy"
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyBusinessUnitService.getBusinessUnit(anyLong())).thenReturn(entity);

        // When: saveBusinessUnit is called on the proxy
        BusinessUnitEntity result = businessUnitServiceProxy.getBusinessUnit(1);

        // Then: legacyBusinessUnitService should be used, and the returned businessUnit should be as expected
        verify(legacyBusinessUnitService).getBusinessUnit(1);
        verifyNoInteractions(opalBusinessUnitService);
        Assertions.assertEquals(entity, result);

        // Given: a businessUnits list result and the app mode is set to "legacy"
        List<BusinessUnitEntity> businessUnitsList = List.of(entity);
        when(legacyBusinessUnitService.searchBusinessUnits(any())).thenReturn(businessUnitsList);

        // When: searchBusinessUnits is called on the proxy
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        List<BusinessUnitEntity> listResult = businessUnitServiceProxy.searchBusinessUnits(criteria);

        // Then: opalBusinessUnitService should be used, and the returned list should be as expected
        verify(legacyBusinessUnitService).searchBusinessUnits(criteria);
        verifyNoInteractions(opalBusinessUnitService);
        Assertions.assertEquals(businessUnitsList, listResult); // Not yet implemented in Legacy mode
    }
}
