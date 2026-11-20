package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureCompatibilityVerifierAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.jms.ServiceBusJmsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@ExtendWith(MockitoExtension.class)
class AllocateTillsQueueListenerTest {

    private static final String PAYLOAD = "{\"till_id\":123}";
    private static final Long TILL_ID = 123L;

    @Mock
    private AllocateTillsQueueConsumerService consumer;

    @Mock
    private ServiceBusReceivedMessage message;

    @Mock
    private ServiceBusReceivedMessageContext context;

    @InjectMocks
    private AllocateTillsQueueListener listener;

    @Test
    void onMessage_delegatesBodyToConsumer() {
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));

        listener.onMessage(context);

        InOrder order = inOrder(consumer, context);
        order.verify(consumer).consume(PAYLOAD);
        order.verify(context).complete();
    }

    @Test
    void onMessage_abandonsWhenTillIsNotProcessing() {
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
        doThrow(new TillNotProcessingException(TILL_ID)).when(consumer).consume(PAYLOAD);

        listener.onMessage(context);

        verify(context).abandon();
        verify(context, never()).complete();
    }

    @Test
    void onMessage_abandonsOtherConsumerFailures() {
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
        IllegalArgumentException failure = new IllegalArgumentException("invalid payload");
        doThrow(failure).when(consumer).consume(PAYLOAD);

        listener.onMessage(context);

        verify(context).abandon();
        verify(context, never()).complete();
    }

    @Test
    void onMessage_propagatesCompletionFailure() {
        when(context.getMessage()).thenReturn(message);
        when(message.getBody()).thenReturn(BinaryData.fromString(PAYLOAD));
        RuntimeException failure = new RuntimeException("Settlement failed");
        doThrow(failure).when(context).complete();

        assertThatThrownBy(() -> listener.onMessage(context)).isSameAs(failure);

        verify(context, never()).abandon();
    }

    @Test
    void configuresProcessorForAllocateTillsQueue() {
        enabledContext().withPropertyValues("spring.cloud.azure.servicebus.processor.auto-startup=false")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(AllocateTillsQueueListener.class);
                assertThat(context).hasSingleBean(AllocateTillsQueueErrorHandler.class);
                assertThat(context).hasSingleBean(ServiceBusRecordMessageListener.class);
                assertThat(context).hasSingleBean(ServiceBusErrorHandler.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
                assertThat(context).hasSingleBean(AllocateTillsQueueConsumerService.class);
                ServiceBusProcessorClient processor = context.getBean(ServiceBusProcessorClient.class);
                assertThat(processor.getQueueName()).isEqualTo("allocate-tills");
                assertThat(processor.isRunning()).isFalse();
            });
    }

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner().withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(AllocateTillsQueueListener.class, AllocateTillsQueueErrorHandler.class)
            .withBean(AllocateTillsQueueConsumerService.class, () -> consumer)
            .withConfiguration(AutoConfigurations.of(AzureGlobalPropertiesAutoConfiguration.class,
                AzureTokenCredentialAutoConfiguration.class, AzureCompatibilityVerifierAutoConfiguration.class,
                AzureServiceBusAutoConfiguration.class, ServiceBusJmsAutoConfiguration.class));
    }

    private ApplicationContextRunner enabledContext() {
        return contextRunner().withPropertyValues("opal.tills.service-bus.consumer-enabled=true",
            "opal.automated-task=", "opal.tills.service-bus.queue-name=allocate-tills");
    }
}
