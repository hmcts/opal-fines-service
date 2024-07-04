package uk.gov.hmcts.opal.scheduler.job.outbound;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.scheduler.service.AllPayArchiveService;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
public class AllPayArchiveJob implements CronJob {

    @Value("${opal.schedule.all-pay-archive-job.cron}")
    private String cronExpression;

    @Value("${opal.schedule.all-pay-archive-job.file-name}")
    private String fileName;

    @Autowired
    private AllPayArchiveService allPayArchiveService;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("Job ** {} ** starting @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

            allPayArchiveService.process(fileName);

            log.info(
                "Job ** {} ** completed.  Next job scheduled @ {}",
                context.getJobDetail().getKey().getName(),
                context.getNextFireTime()
            );
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
