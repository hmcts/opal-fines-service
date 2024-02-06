package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyCourtService;
import uk.gov.hmcts.opal.service.opal.CourtService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CourtServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private CourtService opalCourtService;

    @Mock
    private LegacyCourtService legacyCourtService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalCourtServiceWhenModeIsNotLegacy() {
        // Given: a CourtEntity and the app mode is set to "opal"
        CourtEntity entity = CourtEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalCourtService.getCourt(anyLong())).thenReturn(entity);

        // When: saveCourt is called on the proxy
        CourtEntity courtResult = courtServiceProxy.getCourt(1);

        // Then: opalCourtService should be used, and the returned court should be as expected
        verify(opalCourtService).getCourt(1);
        verifyNoInteractions(legacyCourtService);
        Assertions.assertEquals(entity, courtResult);

        // Given: a courts list result and the app mode is set to "opal"
        List<CourtEntity> courtsList = List.of(entity);
        when(opalCourtService.searchCourts(any())).thenReturn(courtsList);

        // When: searchCourts is called on the proxy
        CourtSearchDto criteria = CourtSearchDto.builder().build();
        List<CourtEntity> listResult = courtServiceProxy.searchCourts(criteria);

        // Then: opalCourtService should be used, and the returned list should be as expected
        verify(opalCourtService).searchCourts(criteria);
        verifyNoInteractions(legacyCourtService);
        Assertions.assertEquals(courtsList, listResult);
    }

    @Test
    void shouldUseLegacyCourtServiceWhenModeIsLegacy() {
        // Given: a CourtEntity and the app mode is set to "legacy"
        CourtEntity entity = CourtEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyCourtService.getCourt(anyLong())).thenReturn(entity);

        // When: saveCourt is called on the proxy
        CourtEntity result = courtServiceProxy.getCourt(1);

        // Then: legacyCourtService should be used, and the returned court should be as expected
        verify(legacyCourtService).getCourt(1);
        verifyNoInteractions(opalCourtService);
        Assertions.assertEquals(entity, result);

        // Given: a courts list result and the app mode is set to "legacy"
        List<CourtEntity> courtsList = List.of(entity);
        when(legacyCourtService.searchCourts(any())).thenReturn(courtsList);

        // When: searchCourts is called on the proxy
        CourtSearchDto criteria = CourtSearchDto.builder().build();
        List<CourtEntity> listResult = courtServiceProxy.searchCourts(criteria);

        // Then: opalCourtService should be used, and the returned list should be as expected
        verify(legacyCourtService).searchCourts(criteria);
        verifyNoInteractions(opalCourtService);
        Assertions.assertEquals(courtsList, listResult); // Not yet implemented in Legacy mode
    }
}
