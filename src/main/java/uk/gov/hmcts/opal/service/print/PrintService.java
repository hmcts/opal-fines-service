package uk.gov.hmcts.opal.service.print;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.print.PrintDefinition;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;
import uk.gov.hmcts.opal.repository.print.PrintDefinitionRepository;
import uk.gov.hmcts.opal.repository.print.PrintJobRepository;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "PrintService")
public class PrintService {

    private final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

    private final PrintDefinitionRepository printDefinitionRepository;

    private final PrintJobRepository printJobRepository;

    private static final int MAX_RETRIES = 3;


    public byte[] generatePdf(PrintJob printJob) {
        // Get print definition from database
        final PrintDefinition printDef = getPrintDefinition(printJob.getDocType(), printJob.getDocVersion());
        // Load XSLT template
        Source xslt = new StreamSource(new StringReader(printDef.getXslt()));

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             StringReader xmlReader = new StringReader(printJob.getXmlData())) {

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer(xslt);

            // Setup input for XSLT transformation
            Source src = new StreamSource(xmlReader);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            return outStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }




    private PrintDefinition getPrintDefinition(String docType, String templateId) {

        return printDefinitionRepository.findByDocTypeAndTemplateId(docType, templateId);
    }

    public UUID savePrintJobs(List<PrintJob> printJobs) {
        UUID batchId = UUID.randomUUID();

        for (PrintJob printJob : printJobs) {
            printJob.setBatchId(batchId);
            printJob.setJobId(UUID.randomUUID());
            printJob.setCreatedAt(LocalDateTime.now());
            printJob.setUpdatedAt(LocalDateTime.now());
            printJob.setStatus(PrintStatus.PENDING);
            printJobRepository.save(printJob);
        }

        return batchId;
    }

    public void processPendingJobs(LocalDateTime cutoffDate) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            try {
                attempt++;
                processJobsWithLock(cutoffDate);
                success = true;
            } catch (Exception e) {
                if (e.getCause() instanceof jakarta.persistence.PessimisticLockException) {
                    log.error("Could not acquire lock, retrying... ({} / {})", attempt, MAX_RETRIES);
                    if (attempt >= MAX_RETRIES) {
                        throw e;  // Exceeded max retries, rethrow exception
                    }
                } else {
                    throw e;  // Non-locking exception, rethrow
                }
            }
        }
    }

    @Transactional
    private void processJobsWithLock(LocalDateTime cutoffDate) {
        List<PrintJob> pendingJobs = printJobRepository.findPendingJobsForUpdate("PENDING", cutoffDate);

        for (PrintJob job : pendingJobs) {
            try {
                // Mark job as in-progress
                job.setStatus(PrintStatus.IN_PROGRESS);
                printJobRepository.save(job);

                // Generate PDF
                byte[] pdfData = generatePdf(job);

                if (pdfData != null) {
                    // Save the PDF to a file (example implementation)
                    savePdfToFile(pdfData, job);

                    // Update job status to complete
                    job.setStatus(PrintStatus.COMPLETED);
                } else {
                    // Update job status to failed
                    job.setStatus(PrintStatus.FAILED);
                }

                printJobRepository.save(job);
            } catch (Exception e) {
                log.error("Error processing job {}", job.getPrintJobId(), e);
                // Handle exceptions and set job status to "failed" if necessary
                job.setStatus(PrintStatus.FAILED);
                printJobRepository.save(job);
            }
        }
    }

    private void savePdfToFile(byte[] pdfData, PrintJob job) {
        // Example implementation to save PDF to a file
        // Save to file system, S3, etc.
        // For demonstration purposes, save to a file in the working directory
        // This would be replaced with actual implementation to save the PDF to a location
        // that can be retrieved by the caller
        String fileName = job.getBatchId() + "_" + job.getJobId() + ".pdf";
        String filePath = new File(fileName).getAbsolutePath();
        // Save to file
        // ...
        log.info("PDF saved to {}", filePath);
    }



}
