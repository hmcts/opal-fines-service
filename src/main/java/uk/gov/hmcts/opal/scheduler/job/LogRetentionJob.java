package uk.gov.hmcts.opal.scheduler.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.repository.LogAuditDetailRepository;
import uk.gov.hmcts.opal.scheduler.model.CronJob;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class LogRetentionJob implements CronJob {

    @Value("${opal.schedule.log-retention-job.cron}")
    private String cronExpression;

    private final LogAuditDetailRepository logAuditDetailRepository;

    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("Job ** {} ** starting @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

            logAuditDetailRepository.deleteExpiredLogAudit();

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
