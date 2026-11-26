package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.nio.charset.StandardCharsets;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@ExtendWith(MockitoExtension.class)
class InterfaceFileQueueListenerTest {

    @Mock
    private TextMessage textMessage;

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

        String payload = "123";

        when(textMessage.getText()).thenReturn(payload);

        listener.onMessage(textMessage);

        verify(interfaceFileProcessorService).process(123L);
        verifyNoMoreInteractions(interfaceFileProcessorService);
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
    void shouldRejectUnsupportedMessageType() {
        Message message = mock(Message.class);

        assertThatThrownBy(() -> listener.onMessage(message))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Message must be of type TextMessage or JmsBytesMessage");

        verifyNoInteractions(interfaceFileProcessorService);
    }

    @ParameterizedTest
    @ValueSource(longs = {123L, Long.MAX_VALUE})
    void shouldProcessBytesMessage(long interfaceFileId) throws Exception {
        JmsBytesMessage message = mock(JmsBytesMessage.class);
        when(message.getBody(byte[].class))
            .thenReturn(Long.toString(interfaceFileId).getBytes(StandardCharsets.UTF_8));

        listener.onMessage(message);

        verify(interfaceFileProcessorService).process(interfaceFileId);
        verifyNoMoreInteractions(interfaceFileProcessorService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldRejectBlankBytesPayload(String payload) throws Exception {
        JmsBytesMessage message = mock(JmsBytesMessage.class);
        when(message.getBody(byte[].class))
            .thenReturn(payload == null ? null : payload.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> listener.onMessage(message))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Interface file message payload is blank");

        verifyNoInteractions(interfaceFileProcessorService);
    }

    @Test
    void shouldRejectInvalidBytesPayload() throws Exception {
        JmsBytesMessage message = mock(JmsBytesMessage.class);
        when(message.getBody(byte[].class)).thenReturn("invalid-json".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> listener.onMessage(message))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unable to parse Interface file message payload");

        verifyNoInteractions(interfaceFileProcessorService);
    }

    @Test
    void shouldPropagateBytesReadFailure() throws Exception {
        JmsBytesMessage message = mock(JmsBytesMessage.class);
        JMSException failure = new JMSException("Cannot read body");
        when(message.getBody(byte[].class)).thenThrow(failure);

        assertThatThrownBy(() -> listener.onMessage(message)).isSameAs(failure);

        verifyNoInteractions(interfaceFileProcessorService);
    }

    @Test
    void shouldPropagateTextReadFailure() throws Exception {
        JMSException failure = new JMSException("Cannot read text");
        when(textMessage.getText()).thenThrow(failure);

        assertThatThrownBy(() -> listener.onMessage(textMessage)).isSameAs(failure);

        verifyNoInteractions(interfaceFileProcessorService);
    }

}
