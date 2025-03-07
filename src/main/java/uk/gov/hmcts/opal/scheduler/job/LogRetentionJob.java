package uk.gov.hmcts.opal.scheduler.job;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.scheduler.service.LogRetentionService;

@Component
@Getter
@Slf4j(topic = "opal.LogRetentionJob")
@DisallowConcurrentExecution
public class LogRetentionJob implements CronJob {

    @Value("${opal.schedule.log-retention-job.cron}")
    private String cronExpression;

    private LogRetentionService logRetentionService;

    @Autowired
    public void setLogRetentionService(LogRetentionService logRetentionService) {
        this.logRetentionService = logRetentionService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        try {
            logRetentionService.deleteExpiredLogAudit();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
