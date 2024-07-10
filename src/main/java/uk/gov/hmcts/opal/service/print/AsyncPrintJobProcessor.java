package uk.gov.hmcts.opal.service.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Service
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
