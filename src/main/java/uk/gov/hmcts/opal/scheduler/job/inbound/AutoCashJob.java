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

    private AutoCashService autoCashService;

    @Autowired
    public void setAutoCashService(AutoCashService autoCashService) {
        this.autoCashService = autoCashService;
    }

    @LogJobExecutionTime
    @Override
    public void execute(JobExecutionContext context) {
        try {
            autoCashService.process(this.fileName);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
