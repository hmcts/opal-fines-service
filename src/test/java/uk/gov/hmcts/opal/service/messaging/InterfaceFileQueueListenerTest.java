package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import tools.jackson.databind.ObjectMapper;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@ExtendWith(MockitoExtension.class)
class InterfaceFileQueueListenerTest {

    @Mock
    private TextMessage textMessage;

    @InjectMocks
    private InterfaceFileQueueListener listener;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InterfaceFileProcessorService interfaceFileProcessorService;

    @Test
    void shouldProcessInterfaceFileMessage() throws Exception {

        String payload = "{\"interfaceFileId\":123}";
        InterfaceFileQueueMessage queueMessage =
            new InterfaceFileQueueMessage(123L);

        when(textMessage.getText()).thenReturn(payload);
        when(objectMapper.readValue(
            payload,
            InterfaceFileQueueMessage.class
        )).thenReturn(queueMessage);

        listener.onMessage(textMessage);

        verify(interfaceFileProcessorService)
            .process(123L);
    }


    @Test
    void shouldThrowExceptionWhenPayloadIsBlank() throws Exception {

        when(textMessage.getText()).thenReturn("");

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Interface file message payload is blank");
    }

    @Test
    void shouldThrowExceptionWhenPayloadCannotBeParsed() throws Exception {

        String payload = "invalid-json";

        when(textMessage.getText()).thenReturn(payload);

        when(objectMapper.readValue(
            payload,
            InterfaceFileQueueMessage.class
        )).thenThrow(new JacksonException("Bad JSON") {});

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
