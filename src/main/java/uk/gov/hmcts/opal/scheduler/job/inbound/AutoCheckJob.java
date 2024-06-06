package uk.gov.hmcts.opal.scheduler.job.inbound;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
public class AutoCheckJob implements CronJob {

    @Value("${opal.schedule.auto-check-job.cron}")
    private String cronExpression;


    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("Job ** {} ** starting @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());


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
