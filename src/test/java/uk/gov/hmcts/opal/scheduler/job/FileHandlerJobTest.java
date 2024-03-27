package uk.gov.hmcts.opal.scheduler.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.util.Date;

import static org.assertj.core.util.DateUtil.now;
import static org.mockito.Mockito.when;

class FileHandlerJobTest {

    @InjectMocks
    private FileHandlerJob fileHandlerJob;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testExecute() throws JobExecutionException {
        JobDetail jobDetail = JobBuilder.newJob(FileHandlerJob.class)
            .withIdentity("FileHandlerJob", "FileHandlerJob")
            .build();

        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        Date now = now();
        when(jobExecutionContext.getFireTime()).thenReturn(now);
        when(jobExecutionContext.getNextFireTime()).thenReturn(now);

        fileHandlerJob.execute(jobExecutionContext);
    }
}
