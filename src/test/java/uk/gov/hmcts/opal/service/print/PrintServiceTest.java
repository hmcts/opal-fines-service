package uk.gov.hmcts.opal.service.print;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.entity.print.PrintDefinition;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;
import uk.gov.hmcts.opal.repository.print.PrintDefinitionRepository;
import uk.gov.hmcts.opal.repository.print.PrintJobRepository;
import uk.gov.hmcts.opal.sftp.SftpOutboundService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrintServiceTest {

    @Mock
    private PrintDefinitionRepository printDefinitionRepository;

    @Mock
    private PrintJobRepository printJobRepository;

    @Mock
    private SftpOutboundService sftpOutboundService;

    @InjectMocks
    private PrintService printService;

    private PrintJob printJob1;
    private PrintJob printJob2;
    private PrintJob printJob;
    private PrintDefinition printDefinition;

    @BeforeEach
    void setUp() {
        // Setup for savePrintJobs test
        printJob1 = new PrintJob();
        printJob1.setXmlData("<xml>Data1</xml>");
        printJob1.setDocType("docType1");
        printJob1.setDocVersion("1.0");

        printJob2 = new PrintJob();
        printJob2.setXmlData("<xml>Data2</xml>");
        printJob2.setDocType("docType2");
        printJob2.setDocVersion("1.0");

        // Setup for generatePdf test
        printJob = new PrintJob();
        printJob.setDocType("docType1");
        printJob.setDocVersion("1.0");
        printJob.setXmlData("<root><element>Test</element></root>");

        printDefinition = new PrintDefinition();
        printDefinition.setDocType("docType1");
        printDefinition.setTemplateId("1.0");
        printDefinition.setXslt("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                                    + "<xsl:template match=\"/\">"
                                    + "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
                                    + "<fo:layout-master-set>"
                                    + "<fo:simple-page-master master-name=\"simple\">"
                                    + "<fo:region-body/>"
                                    + "</fo:simple-page-master>"
                                    + "</fo:layout-master-set>"
                                    + "<fo:page-sequence master-reference=\"simple\">"
                                    + "<fo:flow flow-name=\"xsl-region-body\">"
                                    + "<fo:block><xsl:value-of select=\"/root/element\"/></fo:block>"
                                    + "</fo:flow>"
                                    + "</fo:page-sequence>"
                                    + "</fo:root>"
                                    + "</xsl:template>"
                                    + "</xsl:stylesheet>");



    }

    @Test
    void testSavePrintJobs() {
        // Arrange
        List<PrintJob> printJobs = Arrays.asList(printJob1, printJob2);

        // Act
        UUID batchId = printService.savePrintJobs(printJobs);

        // Assert
        assertEquals(printJob1.getBatchId(), batchId);
        assertEquals(printJob2.getBatchId(), batchId);
        assertEquals(PrintStatus.PENDING, printJob1.getStatus());
        assertEquals(PrintStatus.PENDING, printJob2.getStatus());

        verify(printJobRepository, times(2)).save(any(PrintJob.class));
    }

    @Test
    void testGeneratePdf() throws Exception {

        // Arrange
        when(printDefinitionRepository.findByDocTypeAndTemplateId("docType1", "1.0"))
            .thenReturn(printDefinition);

        // Act
        byte[] pdfBytes = printService.generatePdf(printJob);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Validate the content of the PDF using PDFBox
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);
            assertTrue(pdfText.contains("Test"));
        }
    }

    @Test
    void testProcessJobsWithLock() {
        // Arrange
        when(printDefinitionRepository.findByDocTypeAndTemplateId("docType1", "1.0"))
            .thenReturn(printDefinition);
        LocalDateTime cutoffDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        when(printJobRepository.findPendingJobsForUpdate(PrintStatus.PENDING, cutoffDate, pageable))
            .thenReturn(new PageImpl<>(Collections.singletonList(printJob)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        doNothing().when(sftpOutboundService).uploadFile(any(byte[].class), anyString(), anyString());
        printService.setPageSize(10);
        // Act
        printService.processJobsWithLock(cutoffDate);

        // Assert
        verify(printJobRepository, atLeastOnce()).findPendingJobsForUpdate(eq(PrintStatus.PENDING), eq(cutoffDate),
                                                                           any(Pageable.class));
        verify(sftpOutboundService, atLeastOnce()).uploadFile(any(byte[].class), anyString(), anyString());
        verify(printJobRepository, atLeastOnce()).save(any(PrintJob.class));
    }

    @Test
    void testProcessPendingJobsSuccess() {
        // Arrange
        PrintService printServiceSpy = Mockito.spy(printService);
        printServiceSpy.setMaxRetries(3);
        LocalDateTime cutoffDate = LocalDateTime.now();
        doNothing().when(printServiceSpy).processJobsWithLock(cutoffDate);

        // Act
        printServiceSpy.processPendingJobs(cutoffDate);

        // Assert
        verify(printServiceSpy, times(1)).processJobsWithLock(cutoffDate);

        // Consider verifying the state of printServiceSpy or its dependencies here
        // to ensure that processPendingJobs has the expected effects.
    }

    @Test
    void testProcessPendingJobsMaxRetriesExceeded() {
        // Arrange
        PrintService printServiceSpy = Mockito.spy(printService);
        printServiceSpy.setMaxRetries(3); // Assuming maxRetries is set to 3
        LocalDateTime cutoffDate = LocalDateTime.now();

        // Simulate failure in processing jobs, causing retries
        doThrow(new jakarta.persistence.PessimisticLockException("could not acquire lock"))
            .when(printServiceSpy).processJobsWithLock(cutoffDate);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            printServiceSpy.processPendingJobs(cutoffDate);
        }, "Expected processPendingJobs to throw RuntimeException after max retries exceeded");

        // Verify that processJobsWithLock was attempted maxRetries times
        verify(printServiceSpy, times(3)).processJobsWithLock(cutoffDate);
    }

}
