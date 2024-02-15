package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyEnforcerService;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EnforcerServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private EnforcerService opalService;

    @Mock
    private LegacyEnforcerService legacyService;

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

    void testMode(EnforcerServiceInterface targetService, EnforcerServiceInterface otherService) {
        testGetEnforcer(targetService, otherService);
        testSearchEnforcers(targetService, otherService);
    }

    void testGetEnforcer(EnforcerServiceInterface targetService, EnforcerServiceInterface otherService) {
        // Given: an EnforcerEntity is returned from the target service
        EnforcerEntity entity = EnforcerEntity.builder().build();
        when(targetService.getEnforcer(anyLong())).thenReturn(entity);

        // When: getEnforcer is called on the proxy
        EnforcerEntity enforcerResult = enforcerServiceProxy.getEnforcer(1);

        // Then: target service should be used, and the returned enforcer should be as expected
        verify(targetService).getEnforcer(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, enforcerResult);
    }

    void testSearchEnforcers(EnforcerServiceInterface targetService, EnforcerServiceInterface otherService) {
        // Given: an enforcers list result is returned from the target service
        EnforcerEntity entity = EnforcerEntity.builder().build();
        List<EnforcerEntity> enforcersList = List.of(entity);
        when(targetService.searchEnforcers(any())).thenReturn(enforcersList);

        // When: searchEnforcers is called on the proxy
        EnforcerSearchDto criteria = EnforcerSearchDto.builder().build();
        List<EnforcerEntity> listResult = enforcerServiceProxy.searchEnforcers(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchEnforcers(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(enforcersList, listResult);
    }

    @Test
    void shouldUseOpalEnforcerServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyEnforcerServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
