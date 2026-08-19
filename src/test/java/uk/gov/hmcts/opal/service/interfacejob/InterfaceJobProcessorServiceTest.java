package uk.gov.hmcts.opal.service.interfacejob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;

@ExtendWith(MockitoExtension.class)
class InterfaceJobProcessorServiceTest {

    private static final Long INTERFACE_JOB_ID = 123L;
    private static final Short BUSINESS_UNIT_ID = 77;

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @InjectMocks
    private InterfaceJobProcessorService interfaceJobProcessorService;

    @Test
    void processPaymentsInJob_isTransactional() throws NoSuchMethodException {
        // Arrange
        Method method = InterfaceJobProcessorService.class.getMethod("processPaymentsInJob", Long.class);

        // Act
        Transactional transactional = method.getAnnotation(Transactional.class);

        // Assert
        assertThat(transactional).isNotNull();
    }

    @Test
    void processPaymentsInJob_returnsTillIdWhenStoredProcedureSucceeds() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs")).thenReturn(456L);

        // Act
        Optional<Long> tillId = interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID);

        // Assert
        assertThat(tillId).contains(456L);
        verify(interfaceJobRepository).findById(INTERFACE_JOB_ID);
        verify(interfaceJobRepository).processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs");
    }

    @Test
    void processPaymentsInJob_returnsEmptyWhenStoredProcedureReturnsNull() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(INTERFACE_JOB_ID, BUSINESS_UNIT_ID,
            "interface-jobs", "interface-jobs")).thenReturn(null);

        // Act
        Optional<Long> tillId = interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID);

        // Assert
        assertThat(tillId).isEmpty();
    }

    @Test
    void processPaymentsInJob_whenJobCannotBeFound_throwsException() {
        // Arrange
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to process interface job 123")
            .hasCauseInstanceOf(IllegalStateException.class)
            .hasRootCauseMessage("Interface job not found with id: 123");
    }

    @Test
    void processPaymentsInJob_whenRepositoryThrows_wrapsException() {
        // Arrange
        InterfaceJobEntity interfaceJob = interfaceJob();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));
        when(interfaceJobRepository.processPaymentsInJob(eq(INTERFACE_JOB_ID), eq(BUSINESS_UNIT_ID),
            eq("interface-jobs"), eq("interface-jobs")))
            .thenThrow(new RuntimeException("db down"));

        // Act / Assert
        assertThatThrownBy(() -> interfaceJobProcessorService.processPaymentsInJob(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Failed to process interface job 123")
            .hasCauseInstanceOf(RuntimeException.class)
            .hasRootCauseMessage("db down");
    }

    private static InterfaceJobEntity interfaceJob() {
        return InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .businessUnit(BusinessUnitEntity.builder()
                .businessUnitId(BUSINESS_UNIT_ID)
                .build())
            .build();
    }
}
