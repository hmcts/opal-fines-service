package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.disco.BusinessUnitServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyBusinessUnitService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BusinessUnitServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private BusinessUnitService opalService;

    @Mock
    private LegacyBusinessUnitService legacyService;

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

    void testMode(BusinessUnitServiceInterface targetService, BusinessUnitServiceInterface otherService) {
        testGetBusinessUnit(targetService, otherService);
        testSearchBusinessUnits(targetService, otherService);
    }

    void testGetBusinessUnit(BusinessUnitServiceInterface targetService, BusinessUnitServiceInterface otherService) {
        // Given: a BusinessUnitEntity is returned from the target service
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        when(targetService.getBusinessUnit(anyShort())).thenReturn(entity);

        // When: getBusinessUnit is called on the proxy
        BusinessUnitEntity businessUnitResult = businessUnitServiceProxy.getBusinessUnit((short)1);

        // Then: target service should be used, and the returned businessUnit should be as expected
        verify(targetService).getBusinessUnit((short)1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, businessUnitResult);
    }

    void testSearchBusinessUnits(BusinessUnitServiceInterface targetService,
                                 BusinessUnitServiceInterface otherService) {
        // Given: a businessUnits list result is returned from the target service
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        List<BusinessUnitEntity> businessUnitsList = List.of(entity);
        when(targetService.searchBusinessUnits(any())).thenReturn(businessUnitsList);

        // When: searchBusinessUnits is called on the proxy
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        List<BusinessUnitEntity> listResult = businessUnitServiceProxy.searchBusinessUnits(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchBusinessUnits(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(businessUnitsList, listResult);
    }

    @Test
    void shouldUseOpalBusinessUnitServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyBusinessUnitServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
