package uk.gov.hmcts.opal.service.print;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.print.PrintDefinition;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;
import uk.gov.hmcts.opal.repository.print.PrintDefinitionRepository;
import uk.gov.hmcts.opal.repository.print.PrintJobRepository;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PrintServiceTest {

    @Mock
    private PrintDefinitionRepository printDefinitionRepository;

    @Mock
    private PrintJobRepository printJobRepository;

    @InjectMocks
    private PrintService printService;

    private PrintJob printJob1;
    private PrintJob printJob2;
    private PrintJob printJob;
    private PrintDefinition printDefinition;

    @BeforeEach
    public void setUp() {
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
    public void testSavePrintJobs() {
        // Arrange
        List<PrintJob> printJobs = Arrays.asList(printJob1, printJob2);

        // Act
        UUID batchId = printService.savePrintJobs(printJobs);

        // Assert
        assertEquals(printJob1.getBatchId(), batchId);
        assertEquals(printJob2.getBatchId(), batchId);
        assertEquals(printJob1.getStatus(), PrintStatus.PENDING);
        assertEquals(printJob2.getStatus(), PrintStatus.PENDING);

        verify(printJobRepository, times(2)).save(any(PrintJob.class));
    }

    @Test
    public void testGeneratePdf() throws Exception {
        // Arrange
        when(printDefinitionRepository.findByDocTypeAndTemplateId(eq("docType1"), eq("1.0")))
            .thenReturn(printDefinition);

        // Act
        byte[] pdfBytes = printService.generatePdf(printJob);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Validate the content of the PDF using PDFBox
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);
            assertTrue(pdfText.contains("Test"));
        }
    }
}
