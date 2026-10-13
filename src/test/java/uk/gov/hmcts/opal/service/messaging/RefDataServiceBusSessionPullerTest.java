package uk.gov.hmcts.opal.service.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@ExtendWith(MockitoExtension.class)
class RefDataServiceBusSessionPullerTest {

    private static final String PAYLOAD = "{\"header\":{\"data_product\":\"LJA\"},\"payload\":{\"records\":[]}}";

    @Mock
    private ServiceBusSessionReceiverClient sessionReceiverClient;

    @Mock
    private RefDataMessageProcessor processor;

    @Mock
    private ServiceBusReceiverClient sessionWorker;

    @Mock
    private ServiceBusReceivedMessage message;

    private RefDataServiceBusSessionPuller handler;

    @BeforeEach
    void setUp() {
        handler = new RefDataServiceBusSessionPuller(sessionReceiverClient, processor);
    }

    @Test
    void completesMessageAfterSuccessfulProcessing() {
        sessionContainsMessage();

        handler.pullSessionMessages();

        verify(processor).processMessage(PAYLOAD);
        verify(sessionWorker).complete(message);
        verify(sessionWorker, never()).abandon(message);
        verify(sessionWorker).close();
    }

    @Test
    void abandonsMessageWhenProcessingFails() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid ref-data message");
        sessionContainsMessage();
        doThrow(exception).when(processor).processMessage(PAYLOAD);

        handler.pullSessionMessages();

        verify(sessionWorker).abandon(message);
        verify(sessionWorker, never()).complete(message);
        verify(sessionWorker).close();
    }

    @Test
    void ignoresServiceBusTimeoutWhenNoSessionIsAvailable() {
        AmqpException timeoutException = new AmqpException(
            true,
            AmqpErrorCondition.TIMEOUT_ERROR,
            "No session",
            null
        );
        when(sessionReceiverClient.acceptNextSession()).thenThrow(
            new ServiceBusException(timeoutException, ServiceBusErrorSource.ACCEPT_SESSION)
        );

        handler.pullSessionMessages();

        verifyNoInteractions(processor);
    }

    private void sessionContainsMessage() {
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
        when(sessionReceiverClient.acceptNextSession()).thenReturn(sessionWorker);
        when(sessionWorker.receiveMessages(10, Duration.ofSeconds(3))).thenReturn(IterableStream.of(List.of(message)));
    }
}
