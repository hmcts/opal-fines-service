package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.service.ReportService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportsApiController Tests")
class ReportsApiControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportsApiController cut;

    @Test
    @DisplayName("Should return 200 with the report DTO returned by the service")
    void getReport_returnsServiceResult() {
        ReportReports expected = createDefaultReportDto();
        when(reportService.getReport(DEFAULT_REPORT_ID, AUTH_HEADER)).thenReturn(expected);

        ResponseEntity<ReportReports> actual = cut.getReport(DEFAULT_REPORT_ID, AUTH_HEADER);

        assertAll(
            () -> assertEquals(HttpStatus.OK, actual.getStatusCode()),
            () -> assertEquals(expected, actual.getBody()),
            () -> verify(reportService).getReport(DEFAULT_REPORT_ID, AUTH_HEADER)
        );
    }
}
