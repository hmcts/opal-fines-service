package uk.gov.hmcts.opal.scheduler.job.inbound;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.scheduler.service.AutoCashService;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
public class AutoCashJob implements CronJob {

    @Value("${opal.schedule.auto-cash-job.cron}")
    private String cronExpression;

    @Value("${opal.schedule.auto-cash-job.file-name}")
    private String fileName;

    @Autowired
    private AutoCashService autoCashService;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("Job ** {} ** starting @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

            autoCashService.process(this.fileName);

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
