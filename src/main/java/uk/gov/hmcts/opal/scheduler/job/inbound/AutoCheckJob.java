package uk.gov.hmcts.opal.scheduler.job.inbound;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.scheduler.aspect.LogJobExecutionTime;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.scheduler.service.AutoCheckService;

@Component
@Getter
@Slf4j
@DisallowConcurrentExecution
public class AutoCheckJob implements CronJob {

    @Value("${opal.schedule.auto-check-job.cron}")
    private String cronExpression;

    @Value("${opal.schedule.auto-check-job.file-name}")
    private String fileName;

    private AutoCheckService autoCheckService;

    @Autowired
    public void setAutoCheckService(AutoCheckService autoCheckService) {
        this.autoCheckService = autoCheckService;
    }

    @LogJobExecutionTime
    @Override
    public void execute(JobExecutionContext context) {
        try {
            autoCheckService.process(fileName);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
