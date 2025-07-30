package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.disco.TillServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyTillService;
import uk.gov.hmcts.opal.disco.opal.TillService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TillServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private TillService opalService;

    @Mock
    private LegacyTillService legacyService;

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

    void testMode(TillServiceInterface targetService, TillServiceInterface otherService) {
        testGetTill(targetService, otherService);
        testSearchTills(targetService, otherService);
    }

    void testGetTill(TillServiceInterface targetService, TillServiceInterface otherService) {
        // Given: a TillEntity is returned from the target service
        TillEntity entity = TillEntity.builder().build();
        when(targetService.getTill(anyLong())).thenReturn(entity);

        // When: getTill is called on the proxy
        TillEntity tillResult = tillServiceProxy.getTill(1);

        // Then: target service should be used, and the returned till should be as expected
        verify(targetService).getTill(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, tillResult);
    }

    void testSearchTills(TillServiceInterface targetService, TillServiceInterface otherService) {
        // Given: a tills list result is returned from the target service
        TillEntity entity = TillEntity.builder().build();
        List<TillEntity> tillsList = List.of(entity);
        when(targetService.searchTills(any())).thenReturn(tillsList);

        // When: searchTills is called on the proxy
        TillSearchDto criteria = TillSearchDto.builder().build();
        List<TillEntity> listResult = tillServiceProxy.searchTills(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchTills(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(tillsList, listResult);
    }

    @Test
    void shouldUseOpalTillServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyTillServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
