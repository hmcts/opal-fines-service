package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyTillService;
import uk.gov.hmcts.opal.service.opal.TillService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TillServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private TillService opalTillService;

    @Mock
    private LegacyTillService legacyTillService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private TillServiceProxy tillServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalTillServiceWhenModeIsNotLegacy() {
        // Given: a TillEntity and the app mode is set to "opal"
        TillEntity entity = TillEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalTillService.getTill(anyLong())).thenReturn(entity);

        // When: saveTill is called on the proxy
        TillEntity tillResult = tillServiceProxy.getTill(1);

        // Then: opalTillService should be used, and the returned till should be as expected
        verify(opalTillService).getTill(1);
        verifyNoInteractions(legacyTillService);
        Assertions.assertEquals(entity, tillResult);

        // Given: a tills list result and the app mode is set to "opal"
        List<TillEntity> tillsList = List.of(entity);
        when(opalTillService.searchTills(any())).thenReturn(tillsList);

        // When: searchTills is called on the proxy
        TillSearchDto criteria = TillSearchDto.builder().build();
        List<TillEntity> listResult = tillServiceProxy.searchTills(criteria);

        // Then: opalTillService should be used, and the returned list should be as expected
        verify(opalTillService).searchTills(criteria);
        verifyNoInteractions(legacyTillService);
        Assertions.assertEquals(tillsList, listResult);
    }

    @Test
    void shouldUseLegacyTillServiceWhenModeIsLegacy() {
        // Given: a TillEntity and the app mode is set to "legacy"
        TillEntity entity = TillEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyTillService.getTill(anyLong())).thenReturn(entity);

        // When: saveTill is called on the proxy
        TillEntity result = tillServiceProxy.getTill(1);

        // Then: legacyTillService should be used, and the returned till should be as expected
        verify(legacyTillService).getTill(1);
        verifyNoInteractions(opalTillService);
        Assertions.assertEquals(entity, result);

        // Given: a tills list result and the app mode is set to "legacy"
        List<TillEntity> tillsList = List.of(entity);
        when(legacyTillService.searchTills(any())).thenReturn(tillsList);

        // When: searchTills is called on the proxy
        TillSearchDto criteria = TillSearchDto.builder().build();
        List<TillEntity> listResult = tillServiceProxy.searchTills(criteria);

        // Then: opalTillService should be used, and the returned list should be as expected
        verify(legacyTillService).searchTills(criteria);
        verifyNoInteractions(opalTillService);
        Assertions.assertEquals(tillsList, listResult); // Not yet implemented in Legacy mode
    }
}
