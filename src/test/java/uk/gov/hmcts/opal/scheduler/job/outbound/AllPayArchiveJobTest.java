package uk.gov.hmcts.opal.scheduler.job.outbound;

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
import uk.gov.hmcts.opal.scheduler.service.AllPayArchiveService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AllPayArchiveJob.class)
class AllPayArchiveJobTest {

    @Autowired
    private AllPayArchiveJob allPayArchiveJob;

    @MockBean
    private AllPayArchiveService allPayArchiveService;

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
        assertNotNull(allPayArchiveJob.getAllPayArchiveService());
    }

    @Test
    @SneakyThrows
    void testExecuteCallsAutoCashService() {
        allPayArchiveJob.execute(jobExecutionContext);

        verify(allPayArchiveService, times(1)).process(fileNameCaptor.capture());
        assertEquals("test.txt", fileNameCaptor.getValue());
    }

    @Test
    @SneakyThrows
    void testExecuteHandlesException() {
        doThrow(new RuntimeException("Test exception")).when(allPayArchiveService).process(anyString());

        allPayArchiveJob.execute(jobExecutionContext);

        verify(allPayArchiveService, times(1)).process(fileNameCaptor.capture());
        assertEquals("test.txt", fileNameCaptor.getValue());
    }
}
