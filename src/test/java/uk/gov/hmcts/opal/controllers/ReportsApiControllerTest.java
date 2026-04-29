package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportDto;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createFullReportDto;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportsApiController reportsApiController;

    @Nested
    @DisplayName("GET /reports/{id} - Success Cases")
    class GetReportSuccessCases {

        @Test
        @DisplayName("Should return 200 with report details when report exists")
        void getReport_Success() {
            // Arrange
            ReportReports reportDto = createDefaultReportDto();
            when(reportService.getReport(DEFAULT_REPORT_ID)).thenReturn(reportDto);

            // Act
            ResponseEntity<ReportReports> response = reportsApiController.getReport(DEFAULT_REPORT_ID);

            // Assert
            assertAll("Verify response",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK"),
                () -> assertNotNull(response.getBody(), "Response body should not be null")
            );

            assertAll("Verify report details",
                () -> assertEquals(DEFAULT_REPORT_ID, response.getBody().getReportId(), "Report ID should match"),
                () -> assertEquals("Operational report (by enforcement)", response.getBody().getReportTitle(),
                    "Report title should match"),
                () -> assertEquals("Operational Reports", response.getBody().getReportGroup(),
                    "Report group should match")
            );

            assertAll("Verify boolean flags",
                () -> assertFalse(response.getBody().getAuditedReport(), "Audited report flag should be false"),
                () -> assertFalse(response.getBody().getSupportsMultipleBusinessUnits(),
                    "Supports multiple BUs flag should be false"),
                () -> assertTrue(response.getBody().getCanManuallyCreate(), "Can manually create flag should be true")
            );

            verify(reportService, times(1)).getReport(DEFAULT_REPORT_ID);
        }

        @Test
        @DisplayName("Should return 200 with all fields populated including optional fields")
        void getReport_WithAllFieldsPopulated() {
            // Arrange
            ReportReports reportDto = createFullReportDto();
            String reportId = reportDto.getReportId();

            when(reportService.getReport(reportId)).thenReturn(reportDto);

            // Act
            ResponseEntity<ReportReports> response = reportsApiController.getReport(reportId);

            // Assert
            assertAll("Verify response status and body",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK"),
                () -> assertNotNull(response.getBody(), "Response body should not be null")
            );

            assertAll("Verify all populated fields",
                () -> assertEquals(reportId, response.getBody().getReportId(), "Report ID should match"),
                () -> assertEquals("REPORTS_VIEW", response.getBody().getPermission(),
                    "Permission should be REPORTS_VIEW"),
                () -> assertTrue(response.getBody().getAuditedReport(), "Audited report flag should be true"),
                () -> assertTrue(response.getBody().getSupportsMultipleBusinessUnits(),
                    "Supports multiple BUs flag should be true"),
                () -> assertTrue(response.getBody().getIsBespokeJourney(), "Is bespoke journey flag should be true"),
                () -> assertEquals("P30D", response.getBody().getRetentionPeriod(), "Retention period should be P30D")
            );

            verify(reportService, times(1)).getReport(reportId);
        }

        @Test
        @DisplayName("Should handle report with custom configuration")
        void getReport_WithCustomConfiguration() {
            // Arrange
            String reportId = "custom_report";
            ReportReports reportDto = createDefaultReportDto();
            reportDto.setReportId(reportId);

            when(reportService.getReport(reportId)).thenReturn(reportDto);

            // Act
            ResponseEntity<ReportReports> response = reportsApiController.getReport(reportId);

            // Assert
            assertAll("Verify custom report",
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> {
                    assert response.getBody() != null;
                    assertEquals(reportId, response.getBody().getReportId());
                }
            );
        }
    }

    @Nested
    @DisplayName("GET /reports/{id} - Error Cases")
    class GetReportErrorCases {

        @Test
        @DisplayName("Should throw EntityNotFoundException when report does not exist")
        void getReport_NotFound_ThrowsException() {
            // Arrange
            String reportId = "non_existent_report";
            when(reportService.getReport(reportId)).thenThrow(new EntityNotFoundException());

            // Act & Assert
            assertAll("Verify exception is thrown",
                () -> assertThrows(EntityNotFoundException.class,
                    () -> reportsApiController.getReport(reportId),
                    "Should throw EntityNotFoundException when report not found"),
                () -> verify(reportService, times(1)).getReport(reportId)
            );
        }
    }
}



