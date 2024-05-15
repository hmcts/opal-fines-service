package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.service.CourtFeeServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyCourtFeeService;
import uk.gov.hmcts.opal.service.opal.CourtFeeService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CourtFeeServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private CourtFeeService opalService;

    @Mock
    private LegacyCourtFeeService legacyService;

    @InjectMocks
    private CourtFeeServiceProxy courtFeeServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(CourtFeeServiceInterface targetService, CourtFeeServiceInterface otherService) {
        testGetCourtFee(targetService, otherService);
        testSearchCourtFees(targetService, otherService);
    }

    void testGetCourtFee(CourtFeeServiceInterface targetService, CourtFeeServiceInterface otherService) {
        // Given: a CourtFeeEntity is returned from the target service
        CourtFeeEntity entity = CourtFeeEntity.builder().build();
        when(targetService.getCourtFee(anyLong())).thenReturn(entity);

        // When: getCourtFee is called on the proxy
        CourtFeeEntity courtFeeResult = courtFeeServiceProxy.getCourtFee(1);

        // Then: target service should be used, and the returned courtFee should be as expected
        verify(targetService).getCourtFee(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, courtFeeResult);
    }

    void testSearchCourtFees(CourtFeeServiceInterface targetService, CourtFeeServiceInterface otherService) {
        // Given: a courtFees list result is returned from the target service
        CourtFeeEntity entity = CourtFeeEntity.builder().build();
        List<CourtFeeEntity> courtFeesList = List.of(entity);
        when(targetService.searchCourtFees(any())).thenReturn(courtFeesList);

        // When: searchCourtFees is called on the proxy
        CourtFeeSearchDto criteria = CourtFeeSearchDto.builder().build();
        List<CourtFeeEntity> listResult = courtFeeServiceProxy.searchCourtFees(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCourtFees(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(courtFeesList, listResult);
    }

    @Test
    void shouldUseOpalCourtFeeServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyCourtFeeServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
