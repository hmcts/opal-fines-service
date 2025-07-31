package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.disco.LogAuditDetailServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyLogAuditDetailService;
import uk.gov.hmcts.opal.disco.opal.LogAuditDetailService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LogAuditDetailServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private LogAuditDetailService opalService;

    @Mock
    private LegacyLogAuditDetailService legacyService;

    @InjectMocks
    private LogAuditDetailServiceProxy logAuditDetailServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(LogAuditDetailServiceInterface targetService, LogAuditDetailServiceInterface otherService) {
        testGetLogAuditDetail(targetService, otherService);
        testSearchLogAuditDetails(targetService, otherService);
    }

    void testGetLogAuditDetail(LogAuditDetailServiceInterface targetService,
                               LogAuditDetailServiceInterface otherService) {
        // Given: a LogAuditDetailEntity is returned from the target service
        LogAuditDetailEntity entity = LogAuditDetailEntity.builder().build();
        when(targetService.getLogAuditDetail(anyLong())).thenReturn(entity);

        // When: getLogAuditDetail is called on the proxy
        LogAuditDetailEntity logAuditDetailResult = logAuditDetailServiceProxy.getLogAuditDetail(1);

        // Then: target service should be used, and the returned logAuditDetail should be as expected
        verify(targetService).getLogAuditDetail(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, logAuditDetailResult);
    }

    void testSearchLogAuditDetails(LogAuditDetailServiceInterface targetService,
                                   LogAuditDetailServiceInterface otherService) {
        // Given: a logAuditDetails list result is returned from the target service
        LogAuditDetailEntity entity = LogAuditDetailEntity.builder().build();
        List<LogAuditDetailEntity> logAuditDetailsList = List.of(entity);
        when(targetService.searchLogAuditDetails(any())).thenReturn(logAuditDetailsList);

        // When: searchLogAuditDetails is called on the proxy
        LogAuditDetailSearchDto criteria = LogAuditDetailSearchDto.builder().build();
        List<LogAuditDetailEntity> listResult = logAuditDetailServiceProxy.searchLogAuditDetails(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchLogAuditDetails(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(logAuditDetailsList, listResult);
    }

    @Test
    void shouldUseOpalLogAuditDetailServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyLogAuditDetailServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
