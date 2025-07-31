package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.disco.ReportInstanceServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportInstanceService;
import uk.gov.hmcts.opal.disco.opal.ReportInstanceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReportInstanceServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ReportInstanceService opalService;

    @Mock
    private LegacyReportInstanceService legacyService;

    @InjectMocks
    private ReportInstanceServiceProxy reportInstanceServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ReportInstanceServiceInterface targetService, ReportInstanceServiceInterface otherService) {
        testGetReportInstance(targetService, otherService);
        testSearchReportInstances(targetService, otherService);
    }

    void testGetReportInstance(ReportInstanceServiceInterface targetService,
                               ReportInstanceServiceInterface otherService) {
        // Given: a ReportInstanceEntity is returned from the target service
        ReportInstanceEntity entity = ReportInstanceEntity.builder().build();
        when(targetService.getReportInstance(anyLong())).thenReturn(entity);

        // When: getReportInstance is called on the proxy
        ReportInstanceEntity reportInstanceResult = reportInstanceServiceProxy.getReportInstance(1);

        // Then: target service should be used, and the returned reportInstance should be as expected
        verify(targetService).getReportInstance(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, reportInstanceResult);
    }

    void testSearchReportInstances(ReportInstanceServiceInterface targetService,
                                   ReportInstanceServiceInterface otherService) {
        // Given: a reportInstances list result is returned from the target service
        ReportInstanceEntity entity = ReportInstanceEntity.builder().build();
        List<ReportInstanceEntity> reportInstancesList = List.of(entity);
        when(targetService.searchReportInstances(any())).thenReturn(reportInstancesList);

        // When: searchReportInstances is called on the proxy
        ReportInstanceSearchDto criteria = ReportInstanceSearchDto.builder().build();
        List<ReportInstanceEntity> listResult = reportInstanceServiceProxy.searchReportInstances(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchReportInstances(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(reportInstancesList, listResult);
    }

    @Test
    void shouldUseOpalReportInstanceServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyReportInstanceServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
