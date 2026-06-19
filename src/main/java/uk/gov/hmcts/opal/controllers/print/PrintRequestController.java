package uk.gov.hmcts.opal.controllers.print;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.entity.print.PrintJobEntity;
import uk.gov.hmcts.opal.service.print.AsyncPrintJobProcessor;
import uk.gov.hmcts.opal.service.print.PrintService;

@RestController
@RequestMapping("/api/print")
@Slf4j(topic = "PrintRequestController")
@Tag(name = "Print Controller")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.common.poc", name = "enabled", havingValue = "true")
public class PrintRequestController {

    private final PrintService printService;

    private final AsyncPrintJobProcessor asyncPrintJobProcessor;

    private final Clock clock;


    @PostMapping(value = "/enqueue-print-jobs", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Enqueues print jobs for a batch of documents")
    public ResponseEntity<String> enqueuePrintJobs(@RequestBody List<PrintJobEntity> printJobEntities) {
        log.debug(":POST:enqueuePrintJobs: received {} documents", printJobEntities.size());

        UUID batchId = printService.savePrintJobs(printJobEntities);

        return ResponseEntity.ok().body(batchId.toString());
    }


    @PostMapping(value = "/generate-pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generates a PDF from a provided print request")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PrintJobEntity printJobEntity) {
        log.debug(":POST:generatePdf: query: \n{}", printJobEntity.toString());

        byte[] response = printService.generatePdf(printJobEntity);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("output.pdf").build());

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping(value = "/process-pending-jobs")
    @Operation(summary = "Processes pending print jobs")
    public ResponseEntity<String> processPendingJobs() {
        log.debug(":POST:processPendingJobs: processing pending print jobs");

        asyncPrintJobProcessor.processPendingJobsAsync(LocalDateTime.now(clock));

        return ResponseEntity.ok().body("OK");
    }

}
