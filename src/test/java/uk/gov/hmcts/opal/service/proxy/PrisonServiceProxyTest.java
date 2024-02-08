package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyPrisonService;
import uk.gov.hmcts.opal.service.opal.PrisonService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PrisonServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private PrisonService opalPrisonService;

    @Mock
    private LegacyPrisonService legacyPrisonService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private PrisonServiceProxy prisonServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalPrisonServiceWhenModeIsNotLegacy() {
        // Given: a PrisonEntity and the app mode is set to "opal"
        PrisonEntity entity = PrisonEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalPrisonService.getPrison(anyLong())).thenReturn(entity);

        // When: savePrison is called on the proxy
        PrisonEntity prisonResult = prisonServiceProxy.getPrison(1);

        // Then: opalPrisonService should be used, and the returned prison should be as expected
        verify(opalPrisonService).getPrison(1);
        verifyNoInteractions(legacyPrisonService);
        Assertions.assertEquals(entity, prisonResult);

        // Given: a prisons list result and the app mode is set to "opal"
        List<PrisonEntity> prisonsList = List.of(entity);
        when(opalPrisonService.searchPrisons(any())).thenReturn(prisonsList);

        // When: searchPrisons is called on the proxy
        PrisonSearchDto criteria = PrisonSearchDto.builder().build();
        List<PrisonEntity> listResult = prisonServiceProxy.searchPrisons(criteria);

        // Then: opalPrisonService should be used, and the returned list should be as expected
        verify(opalPrisonService).searchPrisons(criteria);
        verifyNoInteractions(legacyPrisonService);
        Assertions.assertEquals(prisonsList, listResult);
    }

    @Test
    void shouldUseLegacyPrisonServiceWhenModeIsLegacy() {
        // Given: a PrisonEntity and the app mode is set to "legacy"
        PrisonEntity entity = PrisonEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyPrisonService.getPrison(anyLong())).thenReturn(entity);

        // When: savePrison is called on the proxy
        PrisonEntity result = prisonServiceProxy.getPrison(1);

        // Then: legacyPrisonService should be used, and the returned prison should be as expected
        verify(legacyPrisonService).getPrison(1);
        verifyNoInteractions(opalPrisonService);
        Assertions.assertEquals(entity, result);

        // Given: a prisons list result and the app mode is set to "legacy"
        List<PrisonEntity> prisonsList = List.of(entity);
        when(legacyPrisonService.searchPrisons(any())).thenReturn(prisonsList);

        // When: searchPrisons is called on the proxy
        PrisonSearchDto criteria = PrisonSearchDto.builder().build();
        List<PrisonEntity> listResult = prisonServiceProxy.searchPrisons(criteria);

        // Then: opalPrisonService should be used, and the returned list should be as expected
        verify(legacyPrisonService).searchPrisons(criteria);
        verifyNoInteractions(opalPrisonService);
        Assertions.assertEquals(prisonsList, listResult); // Not yet implemented in Legacy mode
    }
}
