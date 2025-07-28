package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.disco.LogActionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyLogActionService;
import uk.gov.hmcts.opal.disco.opal.LogActionService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LogActionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private LogActionService opalService;

    @Mock
    private LegacyLogActionService legacyService;

    @InjectMocks
    private LogActionServiceProxy logActionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(LogActionServiceInterface targetService, LogActionServiceInterface otherService) {
        testGetLogAction(targetService, otherService);
        testSearchLogActions(targetService, otherService);
    }

    void testGetLogAction(LogActionServiceInterface targetService, LogActionServiceInterface otherService) {
        // Given: a LogActionEntity is returned from the target service
        LogActionEntity entity = LogActionEntity.builder().build();
        when(targetService.getLogAction(anyShort())).thenReturn(entity);

        // When: getLogAction is called on the proxy
        LogActionEntity logActionResult = logActionServiceProxy.getLogAction((short)1);

        // Then: target service should be used, and the returned logAction should be as expected
        verify(targetService).getLogAction((short)1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, logActionResult);
    }

    void testSearchLogActions(LogActionServiceInterface targetService, LogActionServiceInterface otherService) {
        // Given: a logActions list result is returned from the target service
        LogActionEntity entity = LogActionEntity.builder().build();
        List<LogActionEntity> logActionsList = List.of(entity);
        when(targetService.searchLogActions(any())).thenReturn(logActionsList);

        // When: searchLogActions is called on the proxy
        LogActionSearchDto criteria = LogActionSearchDto.builder().build();
        List<LogActionEntity> listResult = logActionServiceProxy.searchLogActions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchLogActions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(logActionsList, listResult);
    }

    @Test
    void shouldUseOpalLogActionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyLogActionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
