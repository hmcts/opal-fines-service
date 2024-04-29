package uk.gov.hmcts.opal.scheduler.job;

import lombok.SneakyThrows;
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
import uk.gov.hmcts.opal.sftp.SftpService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import static org.assertj.core.util.DateUtil.now;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileHandlerJobTest {

    @InjectMocks
    private FileHandlerJob fileHandlerJob;

    @Mock
    JobExecutionContext jobExecutionContext;

    @Mock
    SftpService sftpService;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testExecute() throws JobExecutionException {
        JobDetail jobDetail = JobBuilder.newJob(FileHandlerJob.class).withIdentity(
            "FileHandlerJob",
            "FileHandlerJob"
        ).build();

        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        Date now = now();
        when(jobExecutionContext.getFireTime()).thenReturn(now);
        when(jobExecutionContext.getNextFireTime()).thenReturn(now);

        fileHandlerJob.execute(jobExecutionContext);

        verify(sftpService, times(1)).uploadOutboundFile(any(), anyString(), anyString());
        verify(sftpService, times(1)).downloadOutboundFile(anyString(), anyString(), any());
    }

    @Test
    @SneakyThrows
    void testProcessInputStream() {
        String inputString = "Test input string";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        fileHandlerJob.logInputStream(inputStream);
    }
}
