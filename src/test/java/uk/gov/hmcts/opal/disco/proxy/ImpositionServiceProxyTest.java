package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.disco.ImpositionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.disco.opal.ImpositionService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ImpositionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ImpositionService opalService;

    @Mock
    private LegacyImpositionService legacyService;

    @InjectMocks
    private ImpositionServiceProxy impositionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ImpositionServiceInterface targetService, ImpositionServiceInterface otherService) {
        testGetImposition(targetService, otherService);
        testSearchImpositions(targetService, otherService);
    }

    void testGetImposition(ImpositionServiceInterface targetService, ImpositionServiceInterface otherService) {
        // Given: a ImpositionEntity is returned from the target service
        ImpositionEntity entity = ImpositionEntity.builder().build();
        when(targetService.getImposition(anyLong())).thenReturn(entity);

        // When: getImposition is called on the proxy
        ImpositionEntity impositionResult = impositionServiceProxy.getImposition(1);

        // Then: target service should be used, and the returned imposition should be as expected
        verify(targetService).getImposition(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, impositionResult);
    }

    void testSearchImpositions(ImpositionServiceInterface targetService, ImpositionServiceInterface otherService) {
        // Given: a impositions list result is returned from the target service
        ImpositionEntity entity = ImpositionEntity.builder().build();
        List<ImpositionEntity> impositionsList = List.of(entity);
        when(targetService.searchImpositions(any())).thenReturn(impositionsList);

        // When: searchImpositions is called on the proxy
        ImpositionSearchDto criteria = ImpositionSearchDto.builder().build();
        List<ImpositionEntity> listResult = impositionServiceProxy.searchImpositions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchImpositions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(impositionsList, listResult);
    }

    @Test
    void shouldUseOpalImpositionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyImpositionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
