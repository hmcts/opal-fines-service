package uk.gov.hmcts.opal.service.print;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.print.PrintDefinition;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;
import uk.gov.hmcts.opal.repository.print.PrintDefinitionRepository;
import uk.gov.hmcts.opal.repository.print.PrintJobRepository;
import uk.gov.hmcts.opal.sftp.SftpLocation;
import uk.gov.hmcts.opal.sftp.SftpOutboundService;

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
@Transactional(transactionManager = "printTransactionManager")
@Setter
@Getter
@RequiredArgsConstructor
@Slf4j(topic = "opal.PrintService")
public class PrintService {

    private final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

    private final PrintDefinitionRepository printDefinitionRepository;

    private final PrintJobRepository printJobRepository;

    private final SftpOutboundService sftpOutboundService;


    @Value("${printservice.maxRetries:3}")
    private int maxRetries;

    @Value("${printservice.pageSize:100}")
    private int pageSize;


    public byte[] generatePdf(PrintJob printJob) {
        // Get print definition from database
        final PrintDefinition printDef = getPrintDefinition(printJob.getDocType(), printJob.getDocVersion());
        // Load XSLT template
        Source xslt = new StreamSource(new StringReader(printDef.getXslt()));

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             StringReader xmlReader = new StringReader(printJob.getXmlData())) {

            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);

            TransformerFactory factory = new TransformerFactoryImpl();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer(xslt);

            // Setup input for XSLT transformation
            Source src = new StreamSource(xmlReader);
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

        log.debug("Saving print jobs for batch {}", batchId);

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

        while (attempt < maxRetries && !success) {
            try {
                attempt++;
                processJobsWithLock(cutoffDate);
                success = true;
            } catch (Exception e) {
                if (e instanceof jakarta.persistence.PessimisticLockException) {
                    log.error("Could not acquire lock, retrying... ({} / {})", attempt, maxRetries);
                    if (attempt >= maxRetries) {
                        throw e;  // Exceeded max retries, rethrow exception
                    }
                } else {
                    throw e;  // Non-locking exception, rethrow
                }
            }
        }
        log.debug("Processed pending jobs");
    }


    protected void processJobsWithLock(LocalDateTime cutoffDate) {
        Pageable pageable = PageRequest.of(0, pageSize);
        log.debug("Page Size: {}", pageSize);
        Page<PrintJob> page;
        do {
            page = this.findPendingJobsForUpdate(PrintStatus.PENDING, cutoffDate, pageable);
            for (PrintJob job : page.getContent()) {
                try {
                    processJob(job);
                } catch (Exception e) {
                    log.error("Error processing job {}", job.getJobId(), e);
                    job.setStatus(PrintStatus.FAILED);
                    printJobRepository.save(job);
                }
            }
            pageable = page.nextPageable();
        } while (page.hasNext());
    }


    private void processJob(PrintJob job) {
        job.setStatus(PrintStatus.IN_PROGRESS);
        printJobRepository.save(job);

        byte[] pdfData = generatePdf(job);

        if (pdfData != null) {
            savePdfToFile(pdfData, job);
            job.setStatus(PrintStatus.COMPLETED);
        } else {
            job.setStatus(PrintStatus.FAILED);
        }

        printJobRepository.save(job);
    }


    private void savePdfToFile(byte[] pdfData, PrintJob job) {
        String fileName = job.getBatchId() + "_" + job.getJobId() + ".pdf";
        log.debug("Saving PDF to file: {}", fileName);

        sftpOutboundService.uploadFile(pdfData, SftpLocation.PRINT_LOCATION.getPath(), fileName);
    }


    private Page<PrintJob> findPendingJobsForUpdate(PrintStatus status, LocalDateTime cutoffDate, Pageable pageable) {
        log.debug("Finding pending jobs for update");
        return printJobRepository.findPendingJobsForUpdate(status, cutoffDate, pageable);
    }
}
