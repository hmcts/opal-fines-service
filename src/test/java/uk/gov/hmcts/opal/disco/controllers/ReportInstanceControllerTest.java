package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.disco.opal.ReportInstanceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportInstanceControllerTest {

    @Mock
    private ReportInstanceService reportInstanceService;

    @InjectMocks
    private ReportInstanceController reportInstanceController;

    @Test
    void testGetReportInstance_Success() {
        // Arrange
        ReportInstanceEntity entity = ReportInstanceEntity.builder().build();

        when(reportInstanceService.getReportInstance(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ReportInstanceEntity> response = reportInstanceController.getReportInstanceById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(reportInstanceService, times(1)).getReportInstance(any(Long.class));
    }

    @Test
    void testSearchReportInstances_Success() {
        // Arrange
        ReportInstanceEntity entity = ReportInstanceEntity.builder().build();
        List<ReportInstanceEntity> reportInstanceList = List.of(entity);

        when(reportInstanceService.searchReportInstances(any())).thenReturn(reportInstanceList);

        // Act
        ReportInstanceSearchDto searchDto = ReportInstanceSearchDto.builder().build();
        ResponseEntity<List<ReportInstanceEntity>> response = reportInstanceController
            .postReportInstancesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reportInstanceList, response.getBody());
        verify(reportInstanceService, times(1)).searchReportInstances(any());
    }

}
