package uk.gov.hmcts.opal.scheduler.job.inbound;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.scheduler.service.AutoCheckService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AutoCheckJob.class)
class AutoCheckJobTest {

    @Autowired
    private AutoCheckJob autoCheckJob;

    @MockBean
    private AutoCheckService autoCheckService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDetail jobDetail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getFireTime()).thenReturn(new java.util.Date());
        when(jobExecutionContext.getNextFireTime()).thenReturn(new java.util.Date());
        when(jobDetail.getKey()).thenReturn(mock(org.quartz.JobKey.class));
        when(jobDetail.getKey().getName()).thenReturn("testJob");
    }

    @Test
    void testAutoCashServiceInjection() {
        assertNotNull(autoCheckJob.getAutoCheckService());
    }

    @Test
    @SneakyThrows
    void testExecuteCallsAutoCheckService() {
        autoCheckJob.execute(jobExecutionContext);

        verify(autoCheckService, times(1)).process("test.txt");
    }

    @Test
    @SneakyThrows
    void testExecuteHandlesException() {
        doThrow(new RuntimeException("Test exception")).when(autoCheckService).process(anyString());

        autoCheckJob.execute(jobExecutionContext);

        verify(autoCheckService, times(1)).process("test.txt");
    }
}
