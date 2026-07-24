package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobFailurePersistenceService;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobProcessorService;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobStatusService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueueProcessingServiceTest {

    @Mock
    private InterfaceJobProcessorService interfaceJobProcessorService;
    @Mock
    private InterfaceJobStatusService interfaceJobStatusService;
    @Mock
    private InterfaceJobFailurePersistenceService interfaceJobFailurePersistenceService;
    @Mock
    private PreAllocatedCashTillService preAllocatedCashTillService;

    @Test
    void processProcessingJob_isTransactional() throws NoSuchMethodException {
        Method method = InterfaceJobQueueProcessingService.class.getMethod("processProcessingJob", Long.class);

        assertThat(method.getAnnotation(Transactional.class)).isNotNull();
    }

    @Test
    void processProcessingJob_marksCompletedWhenTillCreated() {
        when(interfaceJobProcessorService.processPaymentsInJob(123L)).thenReturn(Optional.of(456L));
        when(preAllocatedCashTillService.createPreAllocatedReportInstance(456L, 0L, "interface-jobs"))
            .thenReturn(789L);

        service().processProcessingJob(123L);

        verify(interfaceJobProcessorService).processPaymentsInJob(123L);
        verify(preAllocatedCashTillService).createPreAllocatedReportInstance(456L, 0L, "interface-jobs");
        verify(interfaceJobStatusService).markCompleted(123L);
    }

    @Test
    void processProcessingJob_marksIgnoredWhenNoTillCreated() {
        when(interfaceJobProcessorService.processPaymentsInJob(123L)).thenReturn(Optional.empty());

        service().processProcessingJob(123L);

        verify(interfaceJobProcessorService).processPaymentsInJob(123L);
        verify(interfaceJobStatusService).markIgnored(123L);
        verifyNoInteractions(preAllocatedCashTillService);
    }

    @Test
    void handleProcessingFailure_marksFailedAndInsertsFailureMessage() {
        IllegalArgumentException exception = new IllegalArgumentException("Cash till report failed");

        service().handleProcessingFailure(123L, exception);

        verify(interfaceJobStatusService).markFailed(123L);
        verify(interfaceJobFailurePersistenceService).insertFailureMessage(123L, exception);
        verifyNoMoreInteractions(interfaceJobStatusService, interfaceJobFailurePersistenceService);
        verifyNoInteractions(interfaceJobProcessorService, preAllocatedCashTillService);
    }

    private InterfaceJobQueueProcessingService service() {
        return new InterfaceJobQueueProcessingService(
            interfaceJobProcessorService,
            interfaceJobStatusService,
            interfaceJobFailurePersistenceService,
            preAllocatedCashTillService
        );
    }
}
