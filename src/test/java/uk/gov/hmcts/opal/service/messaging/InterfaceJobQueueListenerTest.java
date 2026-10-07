package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobProcessingException;

@ExtendWith(MockitoExtension.class)
class InterfaceJobQueueListenerTest {

    public static final String MESSAGE_PAYLOAD = "{\"interface_job_id\":123}";

    @Mock
    private TextMessage textMessage;

    @Mock
    private Message message;

    @Mock
    private InterfaceJobQueueConsumerService consumer;

    @InjectMocks
    private InterfaceJobQueueListener listener;

    @Test
    void onMessage_delegatesMessageToConsumer() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(MESSAGE_PAYLOAD);

        // Act
        listener.onMessage(textMessage);

        // Assert
        verify(consumer).consume(MESSAGE_PAYLOAD);
    }

    @Test
    void onMessage_propagatesPaymentFailure() throws JMSException {
        InterfaceJobProcessingException failure = new InterfaceJobProcessingException(123L,
            new IllegalStateException("Missing transformed JSON id"));
        when(textMessage.getText()).thenReturn(MESSAGE_PAYLOAD);
        doThrow(failure).when(consumer).consume(MESSAGE_PAYLOAD);

        assertThatThrownBy(() -> listener.onMessage(textMessage))
            .isSameAs(failure);
    }

    @Test
    void onMessage_messageIsNotText_throwException() {
        // Arrange

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message));

        // Assert
        verifyNoInteractions(consumer);
    }
}
