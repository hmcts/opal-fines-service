package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.BUSINESS_UNITS;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.FROM_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.TO_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.USER_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createDefaultReportInstanceDto;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.service.report.GenericReportService;

@ExtendWith(MockitoExtension.class)
class ReportInstancesApiControllerTest {

    @Mock
    private GenericReportService genericReportService;

    @InjectMocks
    private ReportInstancesApiController controller;

    @Test
    void getReportInstances_withAllParams_returnsNormalServiceResult() {
        ReportInstanceListReportsInner dto = createDefaultReportInstanceDto();

        when(genericReportService
            .searchReportInstances(FROM_DATE, TO_DATE, BUSINESS_UNITS, USER_ID, DEFAULT_REPORT_ID))
            .thenReturn(List.of(dto));

        ResponseEntity<List<ReportInstanceListReportsInner>> response =
            controller.getReportInstances(FROM_DATE, TO_DATE, BUSINESS_UNITS, USER_ID, DEFAULT_REPORT_ID);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> {
                assert response.getBody() != null;
                assertEquals(1, response.getBody().size());
            },
            () -> {
                assert response.getBody() != null;
                assertEquals(dto, response.getBody().getFirst());
            },
            () -> verify(genericReportService)
                .searchReportInstances(FROM_DATE, TO_DATE, BUSINESS_UNITS, USER_ID, DEFAULT_REPORT_ID)
        );
    }

    @Test
    void getReportInstances_withNoParams_returnsEmptyServiceResult() {
        when(genericReportService.searchReportInstances(null, null, null, null, null))
            .thenReturn(List.of());

        ResponseEntity<List<ReportInstanceListReportsInner>> response =
            controller.getReportInstances(null, null, null, null, null);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(List.of(), response.getBody()),
            () -> verify(genericReportService).searchReportInstances(null, null, null, null, null)
        );
    }
}

