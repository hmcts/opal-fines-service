package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.disco.opal.ReportEntryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportEntryControllerTest {

    @Mock
    private ReportEntryService reportEntryService;

    @InjectMocks
    private ReportEntryController reportEntryController;

    @Test
    void testGetReportEntry_Success() {
        // Arrange
        ReportEntryEntity entity = ReportEntryEntity.builder().build();

        when(reportEntryService.getReportEntry(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ReportEntryEntity> response = reportEntryController.getReportEntryById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(reportEntryService, times(1)).getReportEntry(any(Long.class));
    }

    @Test
    void testSearchReportEntries_Success() {
        // Arrange
        ReportEntryEntity entity = ReportEntryEntity.builder().build();
        List<ReportEntryEntity> reportEntryList = List.of(entity);

        when(reportEntryService.searchReportEntries(any())).thenReturn(reportEntryList);

        // Act
        ReportEntrySearchDto searchDto = ReportEntrySearchDto.builder().build();
        ResponseEntity<List<ReportEntryEntity>> response = reportEntryController.postReportEntriesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reportEntryList, response.getBody());
        verify(reportEntryService, times(1)).searchReportEntries(any());
    }

}
