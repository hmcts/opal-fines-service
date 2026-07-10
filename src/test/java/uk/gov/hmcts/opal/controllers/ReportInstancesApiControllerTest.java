package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.parseMediaType;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.FileType.XML;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.BUSINESS_UNITS;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.FROM_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.TO_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.USER_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createDefaultReportInstanceDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.GenericReportService;

@ExtendWith(MockitoExtension.class)
class ReportInstancesApiControllerTest {

    private static final Long REPORT_INSTANCE_ID = 123L;

    @Mock
    private GenericReportService genericReportService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReportInstancesApiController reportInstancesApiController;

    @Nested
    class GetReportInstances {

        @Test
        void whenAllParamsProvided_returnsNormalServiceResult_happyPath() {
            ReportInstanceListReportsInner dto = createDefaultReportInstanceDto();
            List<Short> requestedBusinessUnits = List.of((short) 10, (short) 20);

            when(genericReportService
                .searchReportInstances(FROM_DATE, TO_DATE, BUSINESS_UNITS, USER_ID, DEFAULT_REPORT_ID))
                .thenReturn(List.of(dto));

            ResponseEntity<List<ReportInstanceListReportsInner>> response =
                reportInstancesApiController.getReportInstances(FROM_DATE, TO_DATE, requestedBusinessUnits, USER_ID,
                    DEFAULT_REPORT_ID);

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
    }

    @Nested
    class GetReportInstanceContent {

        @Test
        void whenAcceptHeaderMissing_returnsJsonResponse_happyPath() {
            Map<String, Object> expected = Map.of("report_data", Map.of("rows", 2));
            mock_reportContentRequest(null, JSON, expected);

            ResponseEntity<Map<String, Object>> actual =
                reportInstancesApiController.getReportInstanceContent(REPORT_INSTANCE_ID);

            assertAll(
                () -> assertEquals(OK, actual.getStatusCode()),
                () -> assertEquals(expected, actual.getBody()),
                () -> verify(genericReportService).getReportInstanceContent(REPORT_INSTANCE_ID, JSON)
            );
        }

        @Test
        void whenCsvAcceptHeader_returnsBinaryResponse_happyPath() {
            byte[] expected = "a,b".getBytes();
            mock_reportContentRequest("application/csv", CSV, expected);

            ResponseEntity<Map<String, Object>> actual =
                reportInstancesApiController.getReportInstanceContent(REPORT_INSTANCE_ID);

            assertBinaryResponse(actual, parseMediaType("application/csv"), expected, CSV);
        }

        @Test
        void whenXmlAcceptHeader_returnsBinaryResponse_happyPath() {
            byte[] expected = "<report/>".getBytes();
            mock_reportContentRequest(APPLICATION_XML_VALUE, XML, expected);

            ResponseEntity<Map<String, Object>> actual =
                reportInstancesApiController.getReportInstanceContent(REPORT_INSTANCE_ID);

            assertBinaryResponse(actual, APPLICATION_XML, expected, XML);
        }

        @Test
        void whenPdfAcceptHeader_returnsBinaryResponse_happyPath() {
            byte[] expected = "%PDF".getBytes();
            mock_reportContentRequest(APPLICATION_PDF.toString(), PDF, expected);

            ResponseEntity<Map<String, Object>> actual =
                reportInstancesApiController.getReportInstanceContent(REPORT_INSTANCE_ID);

            assertBinaryResponse(actual, APPLICATION_PDF, expected, PDF);
        }

        @Test
        void whenAcceptHeaderUnsupported_throwsNotAcceptable_sadPath() {
            when(request.getHeader(ACCEPT)).thenReturn("text/plain");

            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reportInstancesApiController.getReportInstanceContent(REPORT_INSTANCE_ID)
            );

            assertAll(
                () -> assertEquals(NOT_ACCEPTABLE, exception.getStatusCode()),
                () -> assertEquals("The requested media type cannot be produced by the server",
                    exception.getReason()),
                () -> verifyNoInteractions(genericReportService)
            );
        }
    }

    private void mock_reportContentRequest(String acceptHeader, FileType fileType, Object expectedContent) {
        when(request.getHeader(ACCEPT)).thenReturn(acceptHeader);
        when(genericReportService.getReportInstanceContent(REPORT_INSTANCE_ID, fileType)).thenReturn(
            expectedContent
        );
    }

    @SuppressWarnings("unchecked")
    private void assertBinaryResponse(
        ResponseEntity<Map<String, Object>> actual,
        MediaType expectedContentType,
        byte[] expectedBody,
        FileType expectedFileType) {
        assertAll(
            () -> assertEquals(OK, actual.getStatusCode()),
            () -> assertEquals(expectedContentType, actual.getHeaders().getContentType()),
            () -> assertArrayEquals(expectedBody, (byte[]) (Object) actual.getBody()),
            () -> verify(genericReportService).getReportInstanceContent(REPORT_INSTANCE_ID, expectedFileType)
        );
    }
}
