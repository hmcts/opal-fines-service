package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportDto;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithNullRetentionPeriod;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.ReportMapper;
import uk.gov.hmcts.opal.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Tests")
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    @Nested
    @DisplayName("getReport() - Success Cases")
    class GetReportSuccessCases {

        @Test
        @DisplayName("Should return report DTO when report exists")
        void getReport_Success() {
            // Arrange
            ReportEntity reportEntity = createDefaultReportEntity();
            ReportReports reportDto = createDefaultReportDto();

            when(reportRepository.findById(DEFAULT_REPORT_ID)).thenReturn(Optional.of(reportEntity));
            when(reportMapper.toDto(reportEntity)).thenReturn(reportDto);

            // Act
            ReportReports result = reportService.getReport(DEFAULT_REPORT_ID);

            // Assert
            assertAll("Verify report DTO",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals(DEFAULT_REPORT_ID, result.getReportId(), "Report ID should match"),
                () -> assertEquals("Operational report (by enforcement)", result.getReportTitle(),
                    "Report title should match"),
                () -> assertEquals("Operational Reports", result.getReportGroup(),
                    "Report group should match")
            );

            assertAll("Verify boolean flags",
                () -> assertFalse(result.getAuditedReport(), "Audited report should be false"),
                () -> assertFalse(result.getSupportsMultipleBusinessUnits(), "Supports multi BU should be false"),
                () -> assertTrue(result.getCanManuallyCreate(), "Can manually create should be true")
            );

            assertAll("Verify repository and mapper interactions",
                () -> verify(reportRepository, times(1)).findById(DEFAULT_REPORT_ID),
                () -> verify(reportMapper, times(1)).toDto(reportEntity)
            );
        }

        @Test
        @DisplayName("Should handle report with null retention period")
        void getReport_WithNullRetentionPeriod() {
            // Arrange
            ReportEntity reportEntity = createReportEntityWithNullRetentionPeriod();
            String reportId = reportEntity.getReportId();

            ReportReports reportDto = createDefaultReportDto();
            reportDto.setReportId(reportId);
            reportDto.setRetentionPeriod(null);

            when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
            when(reportMapper.toDto(reportEntity)).thenReturn(reportDto);

            // Act
            ReportReports result = reportService.getReport(reportId);

            // Assert
            assertAll("Verify report with null retention period",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals(reportId, result.getReportId(), "Report ID should match"),
                () -> assertNull(result.getRetentionPeriod(), "Retention period should be null")
            );

            assertAll("Verify interactions",
                () -> verify(reportRepository, times(1)).findById(reportId),
                () -> verify(reportMapper, times(1)).toDto(reportEntity)
            );
        }
    }

    @Nested
    @DisplayName("getReport() - Error Cases")
    class GetReportErrorCases {

        @Test
        @DisplayName("Should throw EntityNotFoundException when report does not exist")
        void getReport_NotFound_ThrowsEntityNotFoundException() {
            // Arrange
            String reportId = "non_existent_report";
            when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.getReport(reportId),
                "Should throw EntityNotFoundException for non-existent report"
            );

            assertAll("Verify exception details",
                () -> assertEquals("Report not found with id: non_existent_report", exception.getMessage(),
                    "Exception message should indicate report not found"),
                () -> verify(reportRepository, times(1)).findById(reportId)
            );
        }
    }
}

