package uk.gov.hmcts.opal.controllers.print;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.disco.print.AsyncPrintJobProcessor;
import uk.gov.hmcts.opal.disco.print.PrintService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/print")
@Slf4j(topic = "PrintRequestController")
@Tag(name = "Print Controller")
@RequiredArgsConstructor
public class PrintRequestController {

    private final PrintService printService;

    private final AsyncPrintJobProcessor asyncPrintJobProcessor;


    @PostMapping(value = "/enqueue-print-jobs", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enqueues print jobs for a batch of documents")
    public ResponseEntity<String> enqueuePrintJobs(@RequestBody List<PrintJob> printJobs) {
        log.debug(":POST:enqueuePrintJobs: received {} documents", printJobs.size());

        UUID batchId = printService.savePrintJobs(printJobs);

        return ResponseEntity.ok().body(batchId.toString());
    }


    @PostMapping(value = "/generate-pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generates a PDF from a provided print request")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PrintJob printJob) {
        log.debug(":POST:generatePdf: query: \n{}", printJob.toString());

        byte[] response = printService.generatePdf(printJob);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("output.pdf").build());

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping(value = "/process-pending-jobs")
    @Operation(summary = "Processes pending print jobs")
    public ResponseEntity<String> processPendingJobs() {
        log.debug(":POST:processPendingJobs: processing pending print jobs");

        asyncPrintJobProcessor.processPendingJobsAsync(LocalDateTime.now());

        return ResponseEntity.ok().body("OK");
    }

}
