package uk.gov.hmcts.opal.scheduler.job.inbound;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.scheduler.service.AutoCashService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AutoCashJob.class)
class AutoCashJobTest {

    @Autowired
    private AutoCashJob autoCashJob;

    @MockBean
    private AutoCashService autoCashService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Captor
    private ArgumentCaptor<String> fileNameCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAutoCashServiceInjection() {
        assertNotNull(autoCashJob.getAutoCashService());
    }

    @Test
    @SneakyThrows
    void testExecuteCallsAutoCashService() {
        autoCashJob.execute(jobExecutionContext);

        verify(autoCashService, times(1)).process(fileNameCaptor.capture());
        assertEquals("test.txt", fileNameCaptor.getValue());
    }

    @Test
    @SneakyThrows
    void testExecuteHandlesException() {
        doThrow(new RuntimeException("Test exception")).when(autoCashService).process(anyString());

        autoCashJob.execute(jobExecutionContext);

        verify(autoCashService, times(1)).process(fileNameCaptor.capture());
        assertEquals("test.txt", fileNameCaptor.getValue());
    }
}
