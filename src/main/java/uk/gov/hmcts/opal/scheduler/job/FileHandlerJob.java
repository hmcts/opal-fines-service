package uk.gov.hmcts.opal.scheduler.job;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
public class FileHandlerJob implements CronJob {

    @Value("${opal.schedule.file-handler-job.cron}")
    private String cronExpression;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        log.info("Job ** {} ** starting @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());
        //TODO: Some logic here to perform the actual task
        log.info(
            "Job ** {} ** completed.  Next job scheduled @ {}",
            context.getJobDetail().getKey().getName(),
            context.getNextFireTime()
        );

    }

}
