package uk.gov.hmcts.opal.service.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(prefix = "opal.common.poc", name = "enabled", havingValue = "true")
public class AsyncPrintJobProcessor {

    private final PrintService printService;

    @Autowired
    public AsyncPrintJobProcessor(PrintService printService) {
        this.printService = printService;
    }

    @Async
    public void processPendingJobsAsync(LocalDateTime cutoffDate) {
        printService.processPendingJobs(cutoffDate);
    }

}
