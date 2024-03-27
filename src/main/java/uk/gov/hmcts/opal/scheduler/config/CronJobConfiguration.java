package uk.gov.hmcts.opal.scheduler.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.opal.scheduler.model.CronJob;
import uk.gov.hmcts.opal.scheduler.model.JobData;
import uk.gov.hmcts.opal.scheduler.service.JobService;

@Slf4j
@Configuration
public class CronJobConfiguration {

    private final JobService jobService;
    private final CronJob[] cronJobs;

    @Autowired
    public CronJobConfiguration(JobService jobService, CronJob... cronJobs) {
        this.jobService = jobService;
        this.cronJobs = cronJobs;
    }

    @PostConstruct
    public void init() {
        try {
            jobService.clearJobs();
        } catch (SchedulerException e) {
            log.error("Error clearing jobs", e);
        }

        for (CronJob cronJob : cronJobs) {

            JobData jobData = JobData.builder()
                .id(cronJob.getClass().getName())
                .jobClass(cronJob.getClass())
                .data(cronJob.getData())
                .build();

            jobService.scheduleJob(jobData, cronJob.getCronExpression());
        }
    }
}
