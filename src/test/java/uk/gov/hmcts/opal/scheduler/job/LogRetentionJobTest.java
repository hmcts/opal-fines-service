package uk.gov.hmcts.opal.scheduler.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.scheduler.service.LogRetentionService;

import java.util.Date;

import static org.assertj.core.util.DateUtil.now;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogRetentionJobTest {

    @InjectMocks
    LogRetentionJob logRetentionJob;

    @Mock
    JobExecutionContext jobExecutionContext;

    @Mock
    LogRetentionService logRetentionService;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCallDeleteExpiredLogAudit_success() {
        JobDetail jobDetail = JobBuilder.newJob(FileHandlerJob.class)
            .withIdentity("LogRetentionJob", "LogRetentionJob")
            .build();

        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        Date now = now();
        when(jobExecutionContext.getFireTime()).thenReturn(now);
        when(jobExecutionContext.getNextFireTime()).thenReturn(now);
        doNothing().when(logRetentionService).deleteExpiredLogAudit();

        logRetentionJob.execute(jobExecutionContext);

        verify(logRetentionService, times(1)).deleteExpiredLogAudit();
    }

    @Test
    void shouldThrowException_whenCalledDeleteExpiredLogAudit() {
        JobDetail jobDetail = JobBuilder.newJob(FileHandlerJob.class)
            .withIdentity("LogRetentionJob", "LogRetentionJob")
            .build();

        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        Date now = now();
        when(jobExecutionContext.getFireTime()).thenReturn(now);
        when(jobExecutionContext.getNextFireTime()).thenReturn(now);
        doThrow(new RuntimeException("Error occurred")).when(logRetentionService).deleteExpiredLogAudit();

        logRetentionJob.execute(jobExecutionContext);

        verify(logRetentionService, times(1)).deleteExpiredLogAudit();
    }
}
