package uk.gov.hmcts.opal.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SampleJob implements Job {

    private final JobService jobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        jobService.executeSampleJob();

        log.info("Next job scheduled @ {}", context.getNextFireTime());
    }
}
