package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import tools.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@ExtendWith(MockitoExtension.class)
class InterfaceFileQueueListenerTest {

    @Mock
    private TextMessage textMessage;

    @InjectMocks
    private InterfaceFileQueueListener listener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InterfaceFileProcessorService interfaceFileProcessorService;

    @BeforeEach
    void setUp() {
        listener = new InterfaceFileQueueListener(
            objectMapper,
            interfaceFileProcessorService
        );
    }

    @Test
    void shouldProcessInterfaceFileMessage() throws Exception {

        String payload = "{\"interfaceFileId\":123}";

        when(textMessage.getText()).thenReturn(payload);

        listener.onMessage(textMessage);

        verify(interfaceFileProcessorService)
            .process(123L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void shouldThrowExceptionWhenPayloadIsBlank(String payload) throws Exception {

        when(textMessage.getText()).thenReturn(payload);

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Interface file message payload is blank");
    }

    @Test
    void shouldThrowExceptionWhenPayloadCannotBeParsed() throws Exception {

        String payload = "invalid-json";

        when(textMessage.getText()).thenReturn(payload);

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unable to parse Interface file message payload");
    }

    @Test
    void shouldThrowExceptionWhenMessageIsNotTextMessage() {
        Message message = mock(Message.class);

        assertThatThrownBy(() -> listener.onMessage(message))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message must be of type TextMessage");
    }

}
