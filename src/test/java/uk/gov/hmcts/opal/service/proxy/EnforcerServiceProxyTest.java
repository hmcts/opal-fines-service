package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyEnforcerService;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnforcerServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private EnforcerService opalEnforcerService;

    @Mock
    private LegacyEnforcerService legacyEnforcerService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private EnforcerServiceProxy enforcerServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalEnforcerServiceWhenModeIsNotLegacy() {
        // Given: a EnforcerEntity and the app mode is set to "opal"
        EnforcerEntity entity = EnforcerEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalEnforcerService.getEnforcer(anyLong())).thenReturn(entity);

        // When: saveEnforcer is called on the proxy
        EnforcerEntity enforcerResult = enforcerServiceProxy.getEnforcer(1);

        // Then: opalEnforcerService should be used, and the returned enforcer should be as expected
        verify(opalEnforcerService).getEnforcer(1);
        verifyNoInteractions(legacyEnforcerService);
        Assertions.assertEquals(entity, enforcerResult);

        // Given: a enforcers list result and the app mode is set to "opal"
        List<EnforcerEntity> enforcersList = List.of(entity);
        when(opalEnforcerService.searchEnforcers(any())).thenReturn(enforcersList);

        // When: searchEnforcers is called on the proxy
        EnforcerSearchDto criteria = EnforcerSearchDto.builder().build();
        List<EnforcerEntity> listResult = enforcerServiceProxy.searchEnforcers(criteria);

        // Then: opalEnforcerService should be used, and the returned list should be as expected
        verify(opalEnforcerService).searchEnforcers(criteria);
        verifyNoInteractions(legacyEnforcerService);
        Assertions.assertEquals(enforcersList, listResult);
    }

    @Test
    void shouldUseLegacyEnforcerServiceWhenModeIsLegacy() {
        // Given: a EnforcerEntity and the app mode is set to "legacy"
        EnforcerEntity entity = EnforcerEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyEnforcerService.getEnforcer(anyLong())).thenReturn(entity);

        // When: saveEnforcer is called on the proxy
        EnforcerEntity result = enforcerServiceProxy.getEnforcer(1);

        // Then: legacyEnforcerService should be used, and the returned enforcer should be as expected
        verify(legacyEnforcerService).getEnforcer(1);
        verifyNoInteractions(opalEnforcerService);
        Assertions.assertEquals(entity, result);

        // Given: a enforcers list result and the app mode is set to "legacy"
        List<EnforcerEntity> enforcersList = List.of(entity);
        when(legacyEnforcerService.searchEnforcers(any())).thenReturn(enforcersList);

        // When: searchEnforcers is called on the proxy
        EnforcerSearchDto criteria = EnforcerSearchDto.builder().build();
        List<EnforcerEntity> listResult = enforcerServiceProxy.searchEnforcers(criteria);

        // Then: opalEnforcerService should be used, and the returned list should be as expected
        verify(legacyEnforcerService).searchEnforcers(criteria);
        verifyNoInteractions(opalEnforcerService);
        Assertions.assertEquals(enforcersList, listResult); // Not yet implemented in Legacy mode
    }
}
