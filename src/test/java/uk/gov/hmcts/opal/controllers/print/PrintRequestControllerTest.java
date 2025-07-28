package uk.gov.hmcts.opal.controllers.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.service.print.AsyncPrintJobProcessor;
import uk.gov.hmcts.opal.service.print.PrintService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PrintRequestControllerTest {

    @Mock
    private PrintService printService;

    @Mock
    private AsyncPrintJobProcessor asyncPrintJobProcessor;

    @InjectMocks
    private PrintRequestController printRequestController;

    @Test
    void testEnqueuePrintJobs() {
        // Arrange
        PrintJob printJob = new PrintJob();
        List<PrintJob> printJobList = Collections.singletonList(printJob);
        UUID batchId = UUID.randomUUID();

        when(printService.savePrintJobs(anyList())).thenReturn(batchId);

        // Act
        ResponseEntity<String> response = printRequestController.enqueuePrintJobs(printJobList);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(batchId.toString(), response.getBody());
        verify(printService, times(1)).savePrintJobs(anyList());
    }

    @Test
    void testGeneratePdf() {
        // Arrange
        PrintJob printJob = new PrintJob();
        byte[] pdfData = new byte[]{1, 2, 3, 4};

        when(printService.generatePdf(any(PrintJob.class))).thenReturn(pdfData);

        // Act
        ResponseEntity<byte[]> response = printRequestController.generatePdf(printJob);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertEquals(pdfData, response.getBody());
        verify(printService, times(1)).generatePdf(any(PrintJob.class));
    }

    @Test
    void testProcessPendingJobs() {
        // Act
        ResponseEntity<String> response = printRequestController.processPendingJobs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
        verify(asyncPrintJobProcessor, times(1)).processPendingJobsAsync(any());
    }
}
