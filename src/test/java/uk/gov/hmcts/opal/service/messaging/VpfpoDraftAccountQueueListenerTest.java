package uk.gov.hmcts.opal.service.messaging;

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
public class VpfpoDraftAccountQueueListenerTest {

    public static final String PAYLOAD = "payload";

    @Mock
    private TextMessage textMessage;

    @Mock
    private Message message;

    @Mock
    private VpfpoDraftAccountConsumerService consumer;

    @InjectMocks
    private VpfpoDraftAccountQueueListener listener;

    @Test
    void onMessage_sendsToConsumer() throws JMSException {
        when(textMessage.getText()).thenReturn(PAYLOAD);
        listener.onMessage(textMessage);
        verify(consumer).consume(PAYLOAD);
    }

    @Test
    void onMessage_throwsWhenNoText() {
        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message));
    }

}
