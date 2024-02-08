package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyEnforcementService;
import uk.gov.hmcts.opal.service.opal.EnforcementService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnforcementServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private EnforcementService opalEnforcementService;

    @Mock
    private LegacyEnforcementService legacyEnforcementService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private EnforcementServiceProxy enforcementServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void shouldUseOpalEnforcementServiceWhenModeIsNotLegacy() {
        // Given: a EnforcementEntity and the app mode is set to "opal"
        EnforcementEntity entity = EnforcementEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalEnforcementService.getEnforcement(anyLong())).thenReturn(entity);

        // When: saveEnforcement is called on the proxy
        EnforcementEntity enforcementResult = enforcementServiceProxy.getEnforcement(1);

        // Then: opalEnforcementService should be used, and the returned enforcement should be as expected
        verify(opalEnforcementService).getEnforcement(1);
        verifyNoInteractions(legacyEnforcementService);
        Assertions.assertEquals(entity, enforcementResult);

        // Given: a enforcements list result and the app mode is set to "opal"
        List<EnforcementEntity> enforcementsList = List.of(entity);
        when(opalEnforcementService.searchEnforcements(any())).thenReturn(enforcementsList);

        // When: searchEnforcements is called on the proxy
        EnforcementSearchDto criteria = EnforcementSearchDto.builder().build();
        List<EnforcementEntity> listResult = enforcementServiceProxy.searchEnforcements(criteria);

        // Then: opalEnforcementService should be used, and the returned list should be as expected
        verify(opalEnforcementService).searchEnforcements(criteria);
        verifyNoInteractions(legacyEnforcementService);
        Assertions.assertEquals(enforcementsList, listResult);
    }

    @Test
    void shouldUseLegacyEnforcementServiceWhenModeIsLegacy() {
        // Given: a EnforcementEntity and the app mode is set to "legacy"
        EnforcementEntity entity = EnforcementEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyEnforcementService.getEnforcement(anyLong())).thenReturn(entity);

        // When: saveEnforcement is called on the proxy
        EnforcementEntity result = enforcementServiceProxy.getEnforcement(1);

        // Then: legacyEnforcementService should be used, and the returned enforcement should be as expected
        verify(legacyEnforcementService).getEnforcement(1);
        verifyNoInteractions(opalEnforcementService);
        Assertions.assertEquals(entity, result);

        // Given: a enforcements list result and the app mode is set to "legacy"
        List<EnforcementEntity> enforcementsList = List.of(entity);
        when(legacyEnforcementService.searchEnforcements(any())).thenReturn(enforcementsList);

        // When: searchEnforcements is called on the proxy
        EnforcementSearchDto criteria = EnforcementSearchDto.builder().build();
        List<EnforcementEntity> listResult = enforcementServiceProxy.searchEnforcements(criteria);

        // Then: opalEnforcementService should be used, and the returned list should be as expected
        verify(legacyEnforcementService).searchEnforcements(criteria);
        verifyNoInteractions(opalEnforcementService);
        Assertions.assertEquals(enforcementsList, listResult); // Not yet implemented in Legacy mode
    }
}
