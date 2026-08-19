package uk.gov.hmcts.opal.service.interfacejob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;

@ExtendWith(MockitoExtension.class)
class InterfaceJobFailurePersistenceServiceTest {

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private InterfaceMessageRepository interfaceMessageRepository;

    @InjectMocks
    private InterfaceJobFailurePersistenceService interfaceJobFailurePersistenceService;

    @Test
    void insertFailureMessage_stripsStoredProcedureDetailsFromPersistedMessage() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of(InterfaceFileEntity.builder()
            .interfaceFileId(456L)
                .build()))
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));

        // Act
        interfaceJobFailurePersistenceService.insertFailureMessage(123L, new RuntimeException(
            "ERROR: invalid input syntax for type bigint: \"abc\"   Where: PL/pgSQL function "
                + "p_int_payments_in(bigint,smallint,character varying,character varying) line "
                + "107 at FOR over SELECT rows"));

        // Assert
        ArgumentCaptor<String> messageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(interfaceMessageRepository).insertInterfaceMessage(
            eq(123L),
            eq("Error"),
            messageTextCaptor.capture(),
            eq(456L),
            isNull(),
            isNull(),
            isNull());

        assertThat(messageTextCaptor.getValue())
            .contains("invalid input syntax for type bigint")
            .doesNotContain("p_int_payments_in")
            .doesNotContain("Where:");
    }

    @Test
    void insertFailureMessage_fallsBackToOuterExceptionMessageWhenRootCauseMessageBlank() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of())
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));

        // Act
        interfaceJobFailurePersistenceService.insertFailureMessage(123L,
            new RuntimeException("outer failure message", new RuntimeException()));

        // Assert
        assertPersistedMessageText("outer failure message");
    }

    @Test
    void insertFailureMessage_fallsBackToRootCauseClassNameWhenMessagesBlank() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of())
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));

        // Act
        interfaceJobFailurePersistenceService.insertFailureMessage(123L,
            new RuntimeException("", new RuntimeException()));

        // Assert
        assertPersistedMessageText("RuntimeException");
    }

    @Test
    void insertFailureMessage_replacesNewlinesWithSpaces() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of())
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));

        // Act
        interfaceJobFailurePersistenceService.insertFailureMessage(123L,
            new RuntimeException("line one\nline two\rline three"));

        // Assert
        assertPersistedMessageText("line one line two line three");
    }

    @Test
    void insertFailureMessage_truncatesMessagesLongerThan500Characters() {
        // Arrange
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of())
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));
        String longMessage = "x".repeat(501);

        // Act
        interfaceJobFailurePersistenceService.insertFailureMessage(123L, new RuntimeException(longMessage));

        // Assert
        ArgumentCaptor<String> messageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(interfaceMessageRepository).insertInterfaceMessage(
            eq(123L),
            eq("Error"),
            messageTextCaptor.capture(),
            isNull(),
            isNull(),
            isNull(),
            isNull());

        assertThat(messageTextCaptor.getValue()).hasSize(500).isEqualTo(longMessage.substring(0, 500));
    }

    private void assertPersistedMessageText(String expectedMessageText) {
        ArgumentCaptor<String> messageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(interfaceMessageRepository).insertInterfaceMessage(
            eq(123L),
            eq("Error"),
            messageTextCaptor.capture(),
            isNull(),
            isNull(),
            isNull(),
            isNull());

        assertThat(messageTextCaptor.getValue()).isEqualTo(expectedMessageText);
    }
}
