package uk.gov.hmcts.opal.service.interfacejob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;

@ExtendWith(MockitoExtension.class)
class InterfaceJobStatusServiceTest {

    private static final Long INTERFACE_JOB_ID = 123L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 30, 10, 15);

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-30T10:15:00Z"), ZoneOffset.UTC);

    @Test
    void isProcessing_returnsTrueWhenJobIsProcessing() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .status(InterfaceJobStatus.PROCESSING)
            .build();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act
        boolean processing = new InterfaceJobStatusService(interfaceJobRepository, clock)
            .isProcessing(INTERFACE_JOB_ID);

        // Assert
        assertThat(processing)
            .isTrue();
    }

    @Test
    void isProcessing_returnsFalseWhenJobIsNotProcessing() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .status(InterfaceJobStatus.COMPLETED)
            .build();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act
        boolean processing = new InterfaceJobStatusService(interfaceJobRepository, clock)
            .isProcessing(INTERFACE_JOB_ID);

        // Assert
        assertThat(processing)
            .isFalse();
    }

    @Test
    void isProcessing_whenJobCannotBeFound_throwsException() {
        // Arrange
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> new InterfaceJobStatusService(interfaceJobRepository, clock)
            .isProcessing(INTERFACE_JOB_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Interface job not found with id: 123");
    }

    @Test
    void markCompleted_updatesStatusAndCompletedDateTime() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .status(InterfaceJobStatus.PROCESSING)
            .build();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act
        new InterfaceJobStatusService(interfaceJobRepository, clock).markCompleted(INTERFACE_JOB_ID);

        // Assert
        assertThat(interfaceJob.getStatus()).isEqualTo(InterfaceJobStatus.COMPLETED);
        assertThat(interfaceJob.getCompletedDateTime()).isEqualTo(NOW);
        verify(interfaceJobRepository).findById(INTERFACE_JOB_ID);
    }

    @Test
    void markIgnored_updatesStatusAndCompletedDateTime() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .status(InterfaceJobStatus.PROCESSING)
            .build();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act
        new InterfaceJobStatusService(interfaceJobRepository, clock).markIgnored(INTERFACE_JOB_ID);

        // Assert
        assertThat(interfaceJob.getStatus()).isEqualTo(InterfaceJobStatus.IGNORED);
        assertThat(interfaceJob.getCompletedDateTime()).isEqualTo(NOW);
        verify(interfaceJobRepository).findById(INTERFACE_JOB_ID);
    }

    @Test
    void markFailed_updatesStatusAndCompletedDateTime() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(INTERFACE_JOB_ID)
            .status(InterfaceJobStatus.PROCESSING)
            .build();
        when(interfaceJobRepository.findById(INTERFACE_JOB_ID)).thenReturn(Optional.of(interfaceJob));

        // Act
        new InterfaceJobStatusService(interfaceJobRepository, clock).markFailed(INTERFACE_JOB_ID);

        // Assert
        assertThat(interfaceJob.getStatus()).isEqualTo(InterfaceJobStatus.FAILED);
        assertThat(interfaceJob.getCompletedDateTime()).isEqualTo(NOW);
        verify(interfaceJobRepository).findById(INTERFACE_JOB_ID);
    }

    @Test
    void markFailed_isRequiresNewTransactional() throws NoSuchMethodException {
        // Arrange
        Method method = InterfaceJobStatusService.class.getMethod("markFailed", Long.class);

        // Act
        Transactional transactional = method.getAnnotation(Transactional.class);

        // Assert
        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }
}
