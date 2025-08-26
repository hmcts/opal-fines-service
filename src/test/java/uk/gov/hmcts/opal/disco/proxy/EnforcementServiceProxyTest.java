package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.disco.EnforcementServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyEnforcementService;
import uk.gov.hmcts.opal.disco.opal.EnforcementService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnforcementServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private EnforcementService opalService;

    @Mock
    private LegacyEnforcementService legacyService;

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

    void testMode(EnforcementServiceInterface targetService, EnforcementServiceInterface otherService) {
        testGetEnforcement(targetService, otherService);
        testSearchEnforcements(targetService, otherService);
    }

    void testGetEnforcement(EnforcementServiceInterface targetService, EnforcementServiceInterface otherService) {
        // Given: an EnforcementEntity is returned from the target service
        EnforcementEntity entity = EnforcementEntity.builder().build();
        when(targetService.getEnforcement(anyLong())).thenReturn(entity);

        // When: getEnforcement is called on the proxy
        EnforcementEntity enforcementResult = enforcementServiceProxy.getEnforcement(1);

        // Then: target service should be used, and the returned enforcement should be as expected
        verify(targetService).getEnforcement(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, enforcementResult);
    }

    void testSearchEnforcements(EnforcementServiceInterface targetService, EnforcementServiceInterface otherService) {
        // Given: an enforcements list result is returned from the target service
        EnforcementEntity entity = EnforcementEntity.builder().build();
        List<EnforcementEntity> enforcementsList = List.of(entity);
        when(targetService.searchEnforcements(any())).thenReturn(enforcementsList);

        // When: searchEnforcements is called on the proxy
        EnforcementSearchDto criteria = EnforcementSearchDto.builder().build();
        List<EnforcementEntity> listResult = enforcementServiceProxy.searchEnforcements(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchEnforcements(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(enforcementsList, listResult);
    }

    @Test
    void shouldUseOpalEnforcementServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyEnforcementServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
