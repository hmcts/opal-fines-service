package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.disco.ControlTotalServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyControlTotalService;
import uk.gov.hmcts.opal.disco.opal.ControlTotalService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ControlTotalServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ControlTotalService opalService;

    @Mock
    private LegacyControlTotalService legacyService;

    @InjectMocks
    private ControlTotalServiceProxy controlTotalServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ControlTotalServiceInterface targetService, ControlTotalServiceInterface otherService) {
        testGetControlTotal(targetService, otherService);
        testSearchControlTotals(targetService, otherService);
    }

    void testGetControlTotal(ControlTotalServiceInterface targetService, ControlTotalServiceInterface otherService) {
        // Given: a ControlTotalEntity is returned from the target service
        ControlTotalEntity entity = ControlTotalEntity.builder().build();
        when(targetService.getControlTotal(anyLong())).thenReturn(entity);

        // When: getControlTotal is called on the proxy
        ControlTotalEntity controlTotalResult = controlTotalServiceProxy.getControlTotal(1);

        // Then: target service should be used, and the returned controlTotal should be as expected
        verify(targetService).getControlTotal(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, controlTotalResult);
    }

    void testSearchControlTotals(ControlTotalServiceInterface targetService,
                                 ControlTotalServiceInterface otherService) {
        // Given: a controlTotals list result is returned from the target service
        ControlTotalEntity entity = ControlTotalEntity.builder().build();
        List<ControlTotalEntity> controlTotalsList = List.of(entity);
        when(targetService.searchControlTotals(any())).thenReturn(controlTotalsList);

        // When: searchControlTotals is called on the proxy
        ControlTotalSearchDto criteria = ControlTotalSearchDto.builder().build();
        List<ControlTotalEntity> listResult = controlTotalServiceProxy.searchControlTotals(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchControlTotals(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(controlTotalsList, listResult);
    }

    @Test
    void shouldUseOpalControlTotalServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyControlTotalServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
