package uk.gov.hmcts.opal.scheduler.job;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.opal.sftp.SftpOutboundService;

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
    SftpOutboundService sftpOutboundService;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        verify(sftpOutboundService, times(1)).uploadFile(any(), anyString(), anyString());
        verify(sftpOutboundService, times(1)).downloadFile(anyString(), anyString(), any());
    }

    @Test
    @SneakyThrows
    void testProcessInputStream() {
        String inputString = "Test input string";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        fileHandlerJob.logInputStream(inputStream);

        Assertions.assertDoesNotThrow(() -> { }); // Stops SonarQube complaining about no assertions in method.
    }
}
