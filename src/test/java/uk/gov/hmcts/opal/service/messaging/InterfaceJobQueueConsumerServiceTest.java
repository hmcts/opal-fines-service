package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobStatusService;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueueConsumerServiceTest {

    @Mock
    private InterfaceJobStatusService interfaceJobStatusService;
    @Mock
    private InterfaceJobQueueProcessingService interfaceJobQueueProcessingService;
    @Mock
    private TransientFailureHelper transientFailureHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void consume_processesWhenJobIsProcessing() {
        when(interfaceJobStatusService.isProcessing(123L)).thenReturn(true);

        assertThatCode(() -> service().consume("{\"interface_job_id\":123}"))
            .doesNotThrowAnyException();

        verify(interfaceJobStatusService).isProcessing(123L);
        verify(interfaceJobQueueProcessingService).processProcessingJob(123L);
    }

    @Test
    void consume_failsWhenJobIsNotProcessing() {
        when(interfaceJobStatusService.isProcessing(123L)).thenReturn(false);

        assertThatThrownBy(() -> service().consume("{\"interface_job_id\":123}"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Interface job 123 is not PROCESSING");

        verify(interfaceJobStatusService).isProcessing(123L);
        verifyNoInteractions(interfaceJobQueueProcessingService);
    }

    @Test
    void consume_marksFailedForNonTransientFailure() {
        when(interfaceJobStatusService.isProcessing(123L)).thenReturn(true);
        doThrow(new IllegalArgumentException("Cash till report failed"))
            .when(interfaceJobQueueProcessingService).processProcessingJob(123L);

        assertThatCode(() -> service().consume("{\"interface_job_id\":123}"))
            .doesNotThrowAnyException();

        verify(interfaceJobStatusService).isProcessing(123L);
        verify(interfaceJobQueueProcessingService).processProcessingJob(123L);
        verify(interfaceJobQueueProcessingService).handleProcessingFailure(eq(123L),
            any(IllegalArgumentException.class));
    }

    @Test
    void consume_rethrowsTransientFailure() {
        DataAccessResourceFailureException failure = new DataAccessResourceFailureException("database unavailable");

        when(interfaceJobStatusService.isProcessing(123L)).thenReturn(true);
        doThrow(failure).when(interfaceJobQueueProcessingService).processProcessingJob(123L);
        when(transientFailureHelper.isTransientFailure(failure)).thenReturn(true);

        assertThatThrownBy(() -> service().consume("{\"interface_job_id\":123}"))
            .isInstanceOf(DataAccessResourceFailureException.class);

        verify(interfaceJobStatusService).isProcessing(123L);
        verify(interfaceJobQueueProcessingService).processProcessingJob(123L);
        verify(transientFailureHelper).isTransientFailure(failure);
        verify(interfaceJobStatusService, never()).markFailed(anyLong());
        verify(interfaceJobQueueProcessingService, never()).handleProcessingFailure(anyLong(),
            any(RuntimeException.class));
    }

    private InterfaceJobQueueConsumerService service() {
        return new InterfaceJobQueueConsumerService(
            objectMapper,
            interfaceJobStatusService,
            interfaceJobQueueProcessingService,
            transientFailureHelper
        );
    }
}
