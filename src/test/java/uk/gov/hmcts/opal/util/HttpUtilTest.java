package uk.gov.hmcts.opal.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.parseMediaType;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.service.report.FileType.PDF;
import static uk.gov.hmcts.opal.service.report.FileType.XML;
import static uk.gov.hmcts.opal.util.HttpUtil.buildReportContentResponse;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class HttpUtilTest {

    @Nested
    class BuildReportContentResponse {

        @Test
        void whenJsonRequested_returnsJsonResponse_happyPath() {
            Map<String, Object> content = Map.of("report_data", Map.of("rows", 2));
            ResponseEntity<Map<String, Object>> response = buildReportContentResponse(JSON, content);

            assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(content, response.getBody()),
                () -> assertNull(response.getHeaders().getContentType())
            );
        }

        @Test
        void whenCsvRequested_returnsCsvResponse_happyPath() {
            byte[] content = "a,b".getBytes(UTF_8);
            ResponseEntity<Map<String, Object>> response = buildReportContentResponse(CSV, content);
            assertBinaryResponse(response, content, parseMediaType("application/csv"));
        }

        @Test
        void whenPdfRequested_returnsPdfResponse_happyPath() {
            byte[] content = "%PDF".getBytes(UTF_8);
            ResponseEntity<Map<String, Object>> response = buildReportContentResponse(PDF, content);
            assertBinaryResponse(response, content, APPLICATION_PDF);
        }

        @Test
        void whenXmlRequested_returnsXmlResponse_happyPath() {
            byte[] content = "<report/>".getBytes(UTF_8);
            ResponseEntity<Map<String, Object>> response = buildReportContentResponse(XML, content);
            assertBinaryResponse(response, content, APPLICATION_XML);
        }

        private void assertBinaryResponse(
            ResponseEntity<Map<String, Object>> response,
            byte[] expectedContent,
            MediaType expectedMediaType
        ) {
            assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertEquals(expectedMediaType, response.getHeaders().getContentType()),
                () -> assertArrayEquals(expectedContent, (byte[]) (Object) response.getBody())
            );
        }
    }
}
