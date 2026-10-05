package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@ExtendWith(MockitoExtension.class)
class RefDataServiceBusMessageConsumerTest {

    private static final String PAYLOAD = "{\"header\":{\"data_product\":\"LJA\"},\"payload\":{\"records\":[]}}";

    @Mock
    private RefDataMessageProcessor processor;

    @Mock
    private ServiceBusReceivedMessageContext context;

    @Mock
    private ServiceBusReceivedMessage message;

    private RefDataServiceBusMessageConsumer handler;

    @BeforeEach
    void setUp() {
        handler = new RefDataServiceBusMessageConsumer(processor);
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
    }

    @Test
    void completesMessageAfterSuccessfulProcessing() {
        handler.consumeMessage(context);

        verify(processor).processMessage(PAYLOAD);
        verify(context).complete();
        verify(context, never()).abandon();
    }

    @Test
    void abandonsMessageAndRethrowsWhenProcessingFails() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid ref-data message");
        doThrow(exception).when(processor).processMessage(PAYLOAD);

        assertThatThrownBy(() -> handler.consumeMessage(context))
            .isSameAs(exception);

        verify(context).abandon();
        verify(context, never()).complete();
    }
}
