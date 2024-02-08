package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyLocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LocalJusticeAreaServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private LocalJusticeAreaService opalLocalJusticeAreaService;

    @Mock
    private LegacyLocalJusticeAreaService legacyLocalJusticeAreaService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private LocalJusticeAreaServiceProxy localJusticeAreaServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalLocalJusticeAreaServiceWhenModeIsNotLegacy() {
        // Given: a LocalJusticeAreaEntity and the app mode is set to "opal"
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalLocalJusticeAreaService.getLocalJusticeArea(anyLong())).thenReturn(entity);

        // When: saveLocalJusticeArea is called on the proxy
        LocalJusticeAreaEntity localJusticeAreaResult = localJusticeAreaServiceProxy.getLocalJusticeArea(1);

        // Then: opalLocalJusticeAreaService should be used, and the returned localJusticeArea should be as expected
        verify(opalLocalJusticeAreaService).getLocalJusticeArea(1);
        verifyNoInteractions(legacyLocalJusticeAreaService);
        Assertions.assertEquals(entity, localJusticeAreaResult);

        // Given: a localJusticeAreas list result and the app mode is set to "opal"
        List<LocalJusticeAreaEntity> localJusticeAreasList = List.of(entity);
        when(opalLocalJusticeAreaService.searchLocalJusticeAreas(any())).thenReturn(localJusticeAreasList);

        // When: searchLocalJusticeAreas is called on the proxy
        LocalJusticeAreaSearchDto criteria = LocalJusticeAreaSearchDto.builder().build();
        List<LocalJusticeAreaEntity> listResult = localJusticeAreaServiceProxy.searchLocalJusticeAreas(criteria);

        // Then: opalLocalJusticeAreaService should be used, and the returned list should be as expected
        verify(opalLocalJusticeAreaService).searchLocalJusticeAreas(criteria);
        verifyNoInteractions(legacyLocalJusticeAreaService);
        Assertions.assertEquals(localJusticeAreasList, listResult);
    }

    @Test
    void shouldUseLegacyLocalJusticeAreaServiceWhenModeIsLegacy() {
        // Given: a LocalJusticeAreaEntity and the app mode is set to "legacy"
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyLocalJusticeAreaService.getLocalJusticeArea(anyLong())).thenReturn(entity);

        // When: saveLocalJusticeArea is called on the proxy
        LocalJusticeAreaEntity result = localJusticeAreaServiceProxy.getLocalJusticeArea(1);

        // Then: legacyLocalJusticeAreaService should be used, and the returned localJusticeArea should be as expected
        verify(legacyLocalJusticeAreaService).getLocalJusticeArea(1);
        verifyNoInteractions(opalLocalJusticeAreaService);
        Assertions.assertEquals(entity, result);

        // Given: a localJusticeAreas list result and the app mode is set to "legacy"
        List<LocalJusticeAreaEntity> localJusticeAreasList = List.of(entity);
        when(legacyLocalJusticeAreaService.searchLocalJusticeAreas(any())).thenReturn(localJusticeAreasList);

        // When: searchLocalJusticeAreas is called on the proxy
        LocalJusticeAreaSearchDto criteria = LocalJusticeAreaSearchDto.builder().build();
        List<LocalJusticeAreaEntity> listResult = localJusticeAreaServiceProxy.searchLocalJusticeAreas(criteria);

        // Then: opalLocalJusticeAreaService should be used, and the returned list should be as expected
        verify(legacyLocalJusticeAreaService).searchLocalJusticeAreas(criteria);
        verifyNoInteractions(opalLocalJusticeAreaService);
        Assertions.assertEquals(localJusticeAreasList, listResult); // Not yet implemented in Legacy mode
    }
}
