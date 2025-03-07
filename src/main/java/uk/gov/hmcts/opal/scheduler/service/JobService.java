package uk.gov.hmcts.opal.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.scheduler.exception.JobException;
import uk.gov.hmcts.opal.scheduler.model.JobData;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

@Service
@Slf4j(topic = "opal.JobService")
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;

    public JobKey scheduleJob(JobData jobData, ZonedDateTime startDateTime) {
        try {
            scheduler.scheduleJob(
                newJob(jobData.getJobClass())
                    .withIdentity(jobData.getId(), jobData.getGroup())
                    .withDescription(jobData.getDescription())
                    .usingJobData(new JobDataMap(jobData.getData()))
                    .requestRecovery()
                    .build(),
                newTrigger()
                    .startAt(Date.from(startDateTime.toInstant()))
                    .withIdentity(jobData.getId(), jobData.getGroup())
                    .withDescription(jobData.getDescription())
                    .withSchedule(
                        simpleSchedule()
                            .withMisfireHandlingInstructionNowWithExistingCount()
                    )
                    .build()
            );

            return JobKey.jobKey(jobData.getId(), jobData.getGroup());

        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling a job", exc);
        }
    }

    public JobKey scheduleJob(JobData jobData, String cronExpression) {
        try {

            JobDetail jobDetail = newJob(jobData.getJobClass())
                .withIdentity(jobData.getId(), jobData.getGroup())
                .withDescription(jobData.getDescription())
                .usingJobData(new JobDataMap(jobData.getData()))
                .requestRecovery()
                .build();

            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.deleteJob(jobDetail.getKey());
            }

            if (StringUtils.isBlank(cronExpression)) {
                log.warn("Trigger for {} not set up as schedule is missing", jobData.getJobClass());
                return null;
            }

            CronTrigger trigger = newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .withIdentity(jobData.getId(), jobData.getGroup())
                .withDescription(jobData.getDescription())
                .build();

            scheduler.scheduleJob(jobDetail, trigger);

            return JobKey.jobKey(jobData.getId(), jobData.getGroup());

        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling a job", exc);
        }
    }

    public void clearJobs() throws SchedulerException {
        scheduler.clear();
    }

    public JobKey rescheduleJob(JobData jobData, ZonedDateTime startDateTime) {
        try {

            TriggerKey triggerKey = triggerKey(jobData.getId(), jobData.getGroup());
            SimpleTrigger newTrigger = newTrigger()
                .startAt(Date.from(startDateTime.toInstant()))
                .withIdentity(jobData.getId(), jobData.getGroup())
                .withDescription(jobData.getDescription())
                .withSchedule(
                    simpleSchedule()
                        .withMisfireHandlingInstructionNowWithExistingCount()
                )
                .build();

            Date rescheduleJob = scheduler.rescheduleJob(triggerKey, newTrigger);

            if (rescheduleJob == null) {
                scheduleJob(jobData, startDateTime);
            }

            return JobKey.jobKey(jobData.getId(), jobData.getGroup());

        } catch (SchedulerException exc) {
            throw new JobException("Error while rescheduling a job", exc);
        }
    }
}
