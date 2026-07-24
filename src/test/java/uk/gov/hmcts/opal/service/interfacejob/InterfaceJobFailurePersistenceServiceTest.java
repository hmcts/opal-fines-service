package uk.gov.hmcts.opal.service.interfacejob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void insertFailureMessage_stripsStoredProcedureDetailsFromPersistedMessage() {
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .interfaceFiles(List.of(InterfaceFileEntity.builder()
                .interfaceFileId(456L)
                .build()))
            .build();
        when(interfaceJobRepository.findById(123L)).thenReturn(Optional.of(interfaceJob));

        new InterfaceJobFailurePersistenceService(interfaceJobRepository, interfaceMessageRepository)
            .insertFailureMessage(123L, new RuntimeException(
                "ERROR: invalid input syntax for type bigint: \"abc\"   Where: PL/pgSQL function "
                    + "p_int_payments_in(bigint,smallint,character varying,character varying) line "
                    + "107 at FOR over SELECT rows"));

        ArgumentCaptor<String> messageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(interfaceMessageRepository).insertInterfaceMessage(
            any(),
            any(),
            messageTextCaptor.capture(),
            any(),
            any(),
            any(),
            any()
        );

        assertThat(messageTextCaptor.getValue())
            .contains("invalid input syntax for type bigint")
            .doesNotContain("p_int_payments_in")
            .doesNotContain("Where:");
    }
}
