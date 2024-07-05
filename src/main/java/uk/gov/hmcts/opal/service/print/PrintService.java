package uk.gov.hmcts.opal.service.print;

//import org.apache.commons.text.StringEscapeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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



    @Value("${printservice.maxRetries:3}")
    private int maxRetries;

    @Value("${printservice.outputDirectory:./output}")
    private String outputDirectory;

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

        log.info("Saving print jobs for batch {}", batchId);

        for (PrintJob printJob : printJobs) {
            printJob.setBatchId(batchId);
            printJob.setJobId(UUID.randomUUID());
            printJob.setCreatedAt(LocalDateTime.now());
            printJob.setUpdatedAt(LocalDateTime.now());
            printJob.setStatus(PrintStatus.PENDING);
           // String escapedXmlData = "'" + StringEscapeUtils.escapeJson(printJob.getXmlData()) + "'";
           // printJob.setXmlData(escapedXmlData);

            log.info("Saving print job {}", printJob.getXmlData());
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
                if (e.getCause() instanceof jakarta.persistence.PessimisticLockException) {
                    log.error("Could not acquire lock, retrying... ({} / {})", attempt, maxRetries);
                    if (attempt >= maxRetries) {
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
        Pageable pageable = PageRequest.of(0, pageSize);

        Page<PrintJob> page;
        do {
            page = printJobRepository.findPendingJobsForUpdate(PrintStatus.PENDING, cutoffDate, pageable);
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
        String fileName = outputDirectory + File.separator + job.getBatchId() + "_" + job.getJobId() + ".pdf";

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pdfData);
            log.info("PDF saved to {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save PDF to {}", fileName, e);
            throw new RuntimeException("Failed to save PDF", e);
        }
    }



}
