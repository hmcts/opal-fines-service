package uk.gov.hmcts.opal.controllers.print;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.print.PrintJobDto;
import uk.gov.hmcts.opal.entity.print.PrintJobEntity;
import uk.gov.hmcts.opal.mapper.print.PrintJobMapper;
import uk.gov.hmcts.opal.service.print.AsyncPrintJobProcessor;
import uk.gov.hmcts.opal.service.print.PrintService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PrintRequestControllerTest {

    @Mock
    private PrintService printService;

    @Mock
    private AsyncPrintJobProcessor asyncPrintJobProcessor;

    @Mock
    private PrintJobMapper printJobMapper;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private PrintRequestController printRequestController;

    @Test
    void testEnqueuePrintJobs() {
        // Arrange
        PrintJobDto printJobDto = new PrintJobDto();
        PrintJobEntity printJobEntity = new PrintJobEntity();
        List<PrintJobDto> printJobDtoList = Collections.singletonList(printJobDto);
        List<PrintJobEntity> printJobEntityList = Collections.singletonList(printJobEntity);
        UUID batchId = UUID.randomUUID();

        when(printJobMapper.toEntities(printJobDtoList)).thenReturn(printJobEntityList);
        when(printService.savePrintJobs(anyList())).thenReturn(batchId);

        // Act
        ResponseEntity<String> response = printRequestController.enqueuePrintJobs(printJobDtoList);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(batchId.toString(), response.getBody());
        verify(printJobMapper, times(1)).toEntities(printJobDtoList);
        verify(printService, times(1)).savePrintJobs(anyList());
    }

    @Test
    void testGeneratePdf() {
        // Arrange
        PrintJobDto printJobDto = new PrintJobDto();
        PrintJobEntity printJobEntity = new PrintJobEntity();
        byte[] pdfData = new byte[]{1, 2, 3, 4};

        when(printJobMapper.toEntity(printJobDto)).thenReturn(printJobEntity);
        when(printService.generatePdf(any(PrintJobEntity.class))).thenReturn(pdfData);

        // Act
        ResponseEntity<byte[]> response = printRequestController.generatePdf(printJobDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertEquals(pdfData, response.getBody());
        verify(printJobMapper, times(1)).toEntity(printJobDto);
        verify(printService, times(1)).generatePdf(any(PrintJobEntity.class));
    }

    @Test
    void testProcessPendingJobs() {
        // Act
        ResponseEntity<String> response = printRequestController.processPendingJobs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
        verify(asyncPrintJobProcessor, times(1))
            .processPendingJobsAsync(eq(LocalDateTime.of(2026, 5, 7, 10, 15)));
    }
}
