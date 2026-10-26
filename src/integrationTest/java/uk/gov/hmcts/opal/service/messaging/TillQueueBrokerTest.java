package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.TillQueueProperties;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Checks actual delivery on a disposable local emulator with no allocation consumers.
 * Enable with TILL_QUEUE_ASB_TEST_ENABLED=true; this test consumes messages from allocate-tills.
 */
@EnabledIfEnvironmentVariable(named = "TILL_QUEUE_ASB_TEST_ENABLED", matches = "true")
class TillQueueBrokerTest {

    private static final String QUEUE = "allocate-tills";
    private static final String CONNECTION_STRING = "Endpoint=sb://localhost/;"
        + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;UseDevelopmentEmulator=true";

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void deliversOrderedBatchesInSeparateSessions() {
        TillQueuePublisher publisher = publisher();

        try (ServiceBusSessionReceiverClient sessions = sessionReceiver()) {
            publisher.publish(List.of(342301L, 342302L));
            List<ServiceBusReceivedMessage> firstBatch = receiveBatch(sessions);
            assertThat(firstBatch).extracting(this::body)
                .containsExactly("{\"till_id\":342301}", "{\"till_id\":342302}");

            publisher.publish(List.of(342303L, 342304L));
            List<ServiceBusReceivedMessage> secondBatch = receiveBatch(sessions);
            assertThat(secondBatch).extracting(this::body)
                .containsExactly("{\"till_id\":342303}", "{\"till_id\":342304}");
            assertThat(secondBatch.getFirst().getSessionId()).isNotEqualTo(firstBatch.getFirst().getSessionId());
        }
    }

    private List<ServiceBusReceivedMessage> receiveBatch(ServiceBusSessionReceiverClient sessions) {
        try (ServiceBusReceiverClient receiver = sessions.acceptNextSession()) {
            List<ServiceBusReceivedMessage> messages = receiver.receiveMessages(3, Duration.ofSeconds(3))
                .stream().toList();
            try {
                assertThat(messages).hasSize(2);
                String sessionId = messages.getFirst().getSessionId();
                assertThat(sessionId).isNotBlank();
                assertThat(messages).extracting(ServiceBusReceivedMessage::getSessionId).containsOnly(sessionId);
                return messages;
            } finally {
                messages.forEach(receiver::complete);
            }
        }
    }

    private TillQueuePublisher publisher() {
        JmsConnectionFactory factory = new JmsConnectionFactory("amqp://localhost:5672?jms.sendTimeout=10000");
        factory.setUsername("RootManageSharedAccessKey");
        factory.setPassword("local");
        JmsTemplate template = new JmsTemplate(factory);
        template.setSessionTransacted(true);
        TillQueueProperties properties = new TillQueueProperties();
        properties.setQueueName(QUEUE);
        return new TillQueuePublisher(template, new ObjectMapper(), properties);
    }

    private ServiceBusSessionReceiverClient sessionReceiver() {
        return new ServiceBusClientBuilder().connectionString(CONNECTION_STRING)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10)).setMaxRetries(0))
            .sessionReceiver().queueName(QUEUE).buildClient();
    }

    private String body(ServiceBusReceivedMessage message) {
        AmqpAnnotatedMessage raw = message.getRawAmqpMessage();
        return switch (raw.getBody().getBodyType()) {
            case VALUE -> String.valueOf(raw.getBody().getValue());
            case DATA -> new String(raw.getBody().getFirstData(), StandardCharsets.UTF_8);
            case SEQUENCE -> throw new IllegalStateException("Expected a text message, not an AMQP sequence");
        };
    }
}
