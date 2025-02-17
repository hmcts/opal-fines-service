package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.service.CourtServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyCourtService;
import uk.gov.hmcts.opal.service.opal.CourtService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CourtServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private CourtService opalService;

    @Mock
    private LegacyCourtService legacyService;

    @InjectMocks
    private CourtServiceProxy courtServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(CourtServiceInterface targetService, CourtServiceInterface otherService) {
        testGetCourt(targetService, otherService);
        testSearchCourts(targetService, otherService);
    }

    void testGetCourt(CourtServiceInterface targetService, CourtServiceInterface otherService) {
        // Given: a CourtEntity is returned from the target service
        CourtEntity.Lite entity = CourtEntity.Lite.builder().build();
        when(targetService.getCourtLite(anyLong())).thenReturn(entity);

        // When: getCourt is called on the proxy
        CourtEntity courtResult = courtServiceProxy.getCourtLite(1);

        // Then: target service should be used, and the returned court should be as expected
        verify(targetService).getCourtLite(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, courtResult);
    }

    void testSearchCourts(CourtServiceInterface targetService, CourtServiceInterface otherService) {
        // Given: a courts list result is returned from the target service
        CourtEntity.Lite entity = CourtEntity.Lite.builder().build();
        List<CourtEntity.Lite> courtsList = List.of(entity);
        when(targetService.searchCourts(any())).thenReturn(courtsList);

        // When: searchCourts is called on the proxy
        CourtSearchDto criteria = CourtSearchDto.builder().build();
        List<CourtEntity.Lite> listResult = courtServiceProxy.searchCourts(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchCourts(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(courtsList, listResult);
    }

    @Test
    void shouldUseOpalCourtServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyCourtServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
