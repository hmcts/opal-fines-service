package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.disco.ReportServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportService;
import uk.gov.hmcts.opal.disco.opal.ReportService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReportServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ReportService opalService;

    @Mock
    private LegacyReportService legacyService;

    @InjectMocks
    private ReportServiceProxy reportServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ReportServiceInterface targetService, ReportServiceInterface otherService) {
        testGetReport(targetService, otherService);
        testSearchReports(targetService, otherService);
    }

    void testGetReport(ReportServiceInterface targetService, ReportServiceInterface otherService) {
        // Given: a ReportEntity is returned from the target service
        ReportEntity entity = ReportEntity.builder().build();
        when(targetService.getReport(anyLong())).thenReturn(entity);

        // When: getReport is called on the proxy
        ReportEntity reportResult = reportServiceProxy.getReport(1);

        // Then: target service should be used, and the returned report should be as expected
        verify(targetService).getReport(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, reportResult);
    }

    void testSearchReports(ReportServiceInterface targetService, ReportServiceInterface otherService) {
        // Given: a reports list result is returned from the target service
        ReportEntity entity = ReportEntity.builder().build();
        List<ReportEntity> reportsList = List.of(entity);
        when(targetService.searchReports(any())).thenReturn(reportsList);

        // When: searchReports is called on the proxy
        ReportSearchDto criteria = ReportSearchDto.builder().build();
        List<ReportEntity> listResult = reportServiceProxy.searchReports(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchReports(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(reportsList, listResult);
    }

    @Test
    void shouldUseOpalReportServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyReportServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
