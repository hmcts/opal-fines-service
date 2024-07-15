package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.service.opal.ReportService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void testGetReport_Success() {
        // Arrange
        ReportEntity entity = ReportEntity.builder().build();

        when(reportService.getReport(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ReportEntity> response = reportController.getReportById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(reportService, times(1)).getReport(any(Long.class));
    }

    @Test
    void testSearchReports_Success() {
        // Arrange
        ReportEntity entity = ReportEntity.builder().build();
        List<ReportEntity> reportList = List.of(entity);

        when(reportService.searchReports(any())).thenReturn(reportList);

        // Act
        ReportSearchDto searchDto = ReportSearchDto.builder().build();
        ResponseEntity<List<ReportEntity>> response = reportController.postReportsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reportList, response.getBody());
        verify(reportService, times(1)).searchReports(any());
    }

}
