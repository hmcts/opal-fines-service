package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.disco.PrisonServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPrisonService;
import uk.gov.hmcts.opal.disco.opal.PrisonService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PrisonServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private PrisonService opalService;

    @Mock
    private LegacyPrisonService legacyService;

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

    void testMode(PrisonServiceInterface targetService, PrisonServiceInterface otherService) {
        testGetPrison(targetService, otherService);
        testSearchPrisons(targetService, otherService);
    }

    void testGetPrison(PrisonServiceInterface targetService, PrisonServiceInterface otherService) {
        // Given: a PrisonEntity is returned from the target service
        PrisonEntity entity = PrisonEntity.builder().build();
        when(targetService.getPrison(anyLong())).thenReturn(entity);

        // When: getPrison is called on the proxy
        PrisonEntity prisonResult = prisonServiceProxy.getPrison(1);

        // Then: target service should be used, and the returned prison should be as expected
        verify(targetService).getPrison(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, prisonResult);
    }

    void testSearchPrisons(PrisonServiceInterface targetService, PrisonServiceInterface otherService) {
        // Given: a prisons list result is returned from the target service
        PrisonEntity entity = PrisonEntity.builder().build();
        List<PrisonEntity> prisonsList = List.of(entity);
        when(targetService.searchPrisons(any())).thenReturn(prisonsList);

        // When: searchPrisons is called on the proxy
        PrisonSearchDto criteria = PrisonSearchDto.builder().build();
        List<PrisonEntity> listResult = prisonServiceProxy.searchPrisons(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchPrisons(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(prisonsList, listResult);
    }

    @Test
    void shouldUseOpalPrisonServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyPrisonServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
