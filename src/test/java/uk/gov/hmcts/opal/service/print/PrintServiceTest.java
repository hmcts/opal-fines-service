package uk.gov.hmcts.opal.service.print;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.entity.print.PrintDefinitionEntity;
import uk.gov.hmcts.opal.entity.print.PrintJobEntity;
import uk.gov.hmcts.opal.entity.print.PrintStatus;
import uk.gov.hmcts.opal.repository.print.PrintDefinitionRepository;
import uk.gov.hmcts.opal.repository.print.PrintJobRepository;

@ExtendWith(MockitoExtension.class)
class PrintServiceTest {

    @Mock
    private PrintDefinitionRepository printDefinitionRepository;

    @Mock
    private PrintJobRepository printJobRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private PrintService printService;

    private PrintJobEntity printJobEntity1;
    private PrintJobEntity printJobEntity2;
    private PrintJobEntity printJobEntity;
    private PrintDefinitionEntity printDefinitionEntity;

    @BeforeEach
    void setUp() {
        // Setup for savePrintJobs test
        printJobEntity1 = new PrintJobEntity();
        printJobEntity1.setXmlData("<xml>Data1</xml>");
        printJobEntity1.setDocType("docType1");
        printJobEntity1.setDocVersion("1.0");

        printJobEntity2 = new PrintJobEntity();
        printJobEntity2.setXmlData("<xml>Data2</xml>");
        printJobEntity2.setDocType("docType2");
        printJobEntity2.setDocVersion("1.0");

        // Setup for generatePdf test
        printJobEntity = new PrintJobEntity();
        printJobEntity.setDocType("docType1");
        printJobEntity.setDocVersion("1.0");
        printJobEntity.setXmlData("<root><element>Test</element></root>");

        printDefinitionEntity = new PrintDefinitionEntity();
        printDefinitionEntity.setDocType("docType1");
        printDefinitionEntity.setTemplateId("1.0");
        printDefinitionEntity.setXslt("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
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
        List<PrintJobEntity> printJobEntities = Arrays.asList(printJobEntity1, printJobEntity2);

        // Act
        UUID batchId = printService.savePrintJobs(printJobEntities);

        // Assert
        assertEquals(printJobEntity1.getBatchId(), batchId);
        assertEquals(printJobEntity2.getBatchId(), batchId);
        assertEquals(PrintStatus.PENDING, printJobEntity1.getStatus());
        assertEquals(PrintStatus.PENDING, printJobEntity2.getStatus());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), printJobEntity1.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), printJobEntity1.getUpdatedAt());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), printJobEntity2.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), printJobEntity2.getUpdatedAt());

        verify(printJobRepository, times(2)).save(any(PrintJobEntity.class));
    }

    @Test
    void testGeneratePdf() throws Exception {

        // Arrange
        when(printDefinitionRepository.findByDocTypeAndTemplateId("docType1", "1.0"))
            .thenReturn(printDefinitionEntity);

        // Act
        byte[] pdfBytes = printService.generatePdf(printJobEntity);

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
            .thenReturn(printDefinitionEntity);
        LocalDateTime cutoffDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        when(printJobRepository.findPendingJobsForUpdate(PrintStatus.PENDING, cutoffDate, pageable))
            .thenReturn(new PageImpl<>(Collections.singletonList(printJobEntity)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        //  doNothing().when(sftpOutboundService).uploadFile(any(byte[].class), anyString(), anyString());
        printService.setPageSize(10);
        // Act
        printService.processJobsWithLock(cutoffDate);

        // Assert
        verify(printJobRepository, atLeastOnce()).findPendingJobsForUpdate(eq(PrintStatus.PENDING), eq(cutoffDate),
                                                                           any(Pageable.class));
        // verify(sftpOutboundService, atLeastOnce()).uploadFile(any(byte[].class), anyString(), anyString());
        verify(printJobRepository, atLeastOnce()).save(any(PrintJobEntity.class));
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
