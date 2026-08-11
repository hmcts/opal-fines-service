package uk.gov.hmcts.opal.service.refdata.framework;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefDataTopicListenerTest {

    private static final String MESSAGE_PAYLOAD = "payload";

    @Mock
    TextMessage textMessage;

    @Mock
    Message message;

    @Mock
    RefDataQueueConsumerService consumer;

    @InjectMocks
    RefDataTopicListener listener;

    @Test
    void onMessage_delegatesMessageToConsumer() throws JMSException {
        when(textMessage.getText()).thenReturn(MESSAGE_PAYLOAD);

        listener.onMessage(textMessage);

        verify(consumer).consume(MESSAGE_PAYLOAD);
    }

    @Test
    void onMessage_messageIsNotText_throwException() {
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message));
    }
}
