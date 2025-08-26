package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.disco.ReportEntryServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyReportEntryService;
import uk.gov.hmcts.opal.disco.opal.ReportEntryService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReportEntryServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ReportEntryService opalService;

    @Mock
    private LegacyReportEntryService legacyService;

    @InjectMocks
    private ReportEntryServiceProxy reportEntryServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ReportEntryServiceInterface targetService, ReportEntryServiceInterface otherService) {
        testGetReportEntry(targetService, otherService);
        testSearchReportEntries(targetService, otherService);
    }

    void testGetReportEntry(ReportEntryServiceInterface targetService, ReportEntryServiceInterface otherService) {
        // Given: a ReportEntryEntity is returned from the target service
        ReportEntryEntity entity = ReportEntryEntity.builder().build();
        when(targetService.getReportEntry(anyLong())).thenReturn(entity);

        // When: getReportEntry is called on the proxy
        ReportEntryEntity reportEntryResult = reportEntryServiceProxy.getReportEntry(1);

        // Then: target service should be used, and the returned reportEntry should be as expected
        verify(targetService).getReportEntry(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, reportEntryResult);
    }

    void testSearchReportEntries(ReportEntryServiceInterface targetService, ReportEntryServiceInterface otherService) {
        // Given: a reportEntrys list result is returned from the target service
        ReportEntryEntity entity = ReportEntryEntity.builder().build();
        List<ReportEntryEntity> reportEntrysList = List.of(entity);
        when(targetService.searchReportEntries(any())).thenReturn(reportEntrysList);

        // When: searchReportEntrys is called on the proxy
        ReportEntrySearchDto criteria = ReportEntrySearchDto.builder().build();
        List<ReportEntryEntity> listResult = reportEntryServiceProxy.searchReportEntries(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchReportEntries(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(reportEntrysList, listResult);
    }

    @Test
    void shouldUseOpalReportEntryServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyReportEntryServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
