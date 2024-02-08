package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyMisDebtorService;
import uk.gov.hmcts.opal.service.opal.MisDebtorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MisDebtorServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private MisDebtorService opalMisDebtorService;

    @Mock
    private LegacyMisDebtorService legacyMisDebtorService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private MisDebtorServiceProxy misDebtorServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalMisDebtorServiceWhenModeIsNotLegacy() {
        // Given: a MisDebtorEntity and the app mode is set to "opal"
        MisDebtorEntity entity = MisDebtorEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalMisDebtorService.getMisDebtor(anyLong())).thenReturn(entity);

        // When: saveMisDebtor is called on the proxy
        MisDebtorEntity misDebtorResult = misDebtorServiceProxy.getMisDebtor(1);

        // Then: opalMisDebtorService should be used, and the returned misDebtor should be as expected
        verify(opalMisDebtorService).getMisDebtor(1);
        verifyNoInteractions(legacyMisDebtorService);
        Assertions.assertEquals(entity, misDebtorResult);

        // Given: a misDebtors list result and the app mode is set to "opal"
        List<MisDebtorEntity> misDebtorsList = List.of(entity);
        when(opalMisDebtorService.searchMisDebtors(any())).thenReturn(misDebtorsList);

        // When: searchMisDebtors is called on the proxy
        MisDebtorSearchDto criteria = MisDebtorSearchDto.builder().build();
        List<MisDebtorEntity> listResult = misDebtorServiceProxy.searchMisDebtors(criteria);

        // Then: opalMisDebtorService should be used, and the returned list should be as expected
        verify(opalMisDebtorService).searchMisDebtors(criteria);
        verifyNoInteractions(legacyMisDebtorService);
        Assertions.assertEquals(misDebtorsList, listResult);
    }

    @Test
    void shouldUseLegacyMisDebtorServiceWhenModeIsLegacy() {
        // Given: a MisDebtorEntity and the app mode is set to "legacy"
        MisDebtorEntity entity = MisDebtorEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyMisDebtorService.getMisDebtor(anyLong())).thenReturn(entity);

        // When: saveMisDebtor is called on the proxy
        MisDebtorEntity result = misDebtorServiceProxy.getMisDebtor(1);

        // Then: legacyMisDebtorService should be used, and the returned misDebtor should be as expected
        verify(legacyMisDebtorService).getMisDebtor(1);
        verifyNoInteractions(opalMisDebtorService);
        Assertions.assertEquals(entity, result);

        // Given: a misDebtors list result and the app mode is set to "legacy"
        List<MisDebtorEntity> misDebtorsList = List.of(entity);
        when(legacyMisDebtorService.searchMisDebtors(any())).thenReturn(misDebtorsList);

        // When: searchMisDebtors is called on the proxy
        MisDebtorSearchDto criteria = MisDebtorSearchDto.builder().build();
        List<MisDebtorEntity> listResult = misDebtorServiceProxy.searchMisDebtors(criteria);

        // Then: opalMisDebtorService should be used, and the returned list should be as expected
        verify(legacyMisDebtorService).searchMisDebtors(criteria);
        verifyNoInteractions(opalMisDebtorService);
        Assertions.assertEquals(misDebtorsList, listResult); // Not yet implemented in Legacy mode
    }
}
