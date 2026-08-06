package uk.gov.hmcts.opal.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jms.core.JmsTemplate;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.exception.InterfaceJobQueueException;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueuePublisherImpl;

/**
 * Verifies interface-job delivery and all-or-none broker rollback against the Azure Service Bus emulator.
 * Set INTERFACE_JOBS_QUEUE_ASB_TEST_ENABLED=true to run these tests. SERVICEBUS_CONNECTION_STRING,
 * SERVICEBUS_INTERFACE_JOBS_QUEUE_NAME and SERVICEBUS_PROTOCOL can override the local defaults.
 */
@EnabledIfEnvironmentVariable(named = "INTERFACE_JOBS_QUEUE_ASB_TEST_ENABLED", matches = "true")
@DisplayName("Interface Job Queue Transaction Integration Tests")
class InterfaceJobQueueTransactionIntegrationTest {

    private static final int MAX_PEEK_MESSAGES = 100;
    private static final int QUEUE_PEEK_ATTEMPTS = 10;
    private static final long QUEUE_PEEK_DELAY_MILLIS = 500;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String DEFAULT_CONNECTION_STRING =
        "Endpoint=sb://localhost/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;"
            + "UseDevelopmentEmulator=true";

    private String connectionString;
    private String queueName;
    private JmsConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        connectionString = environmentOrDefault("SERVICEBUS_CONNECTION_STRING", DEFAULT_CONNECTION_STRING);
        queueName = environmentOrDefault("SERVICEBUS_INTERFACE_JOBS_QUEUE_NAME",
            "auto-payments-process-interface-files");
        String protocol = environmentOrDefault("SERVICEBUS_PROTOCOL", "amqp");
        ServiceBusConnectionStringParser.ConnectionDetails details =
            new ServiceBusConnectionStringParser().parse(connectionString);

        connectionFactory = new JmsConnectionFactory("%s://%s".formatted(protocol, details.fullyQualifiedNamespace()));
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());
    }

    @Test
    @DisplayName("PO-2593 - The interface-job publisher sends one message per job")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void publisherSendsOneMessagePerJobToTheRealQueue() {
        // Arrange
        final long firstJobId = System.currentTimeMillis();
        final long secondJobId = firstJobId + 1;

        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDeliveryPersistent(true);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setSessionTransacted(true);

        InterfaceJobServiceBusProperties properties = new InterfaceJobServiceBusProperties();
        properties.setQueueName(queueName);
        InterfaceJobQueuePublisherImpl publisher = new InterfaceJobQueuePublisherImpl(jmsTemplate, new ObjectMapper(),
            properties);

        // Act
        publisher.publish(List.of(firstJobId, secondJobId));

        // Assert
        assertTrue(queueContainsJob(firstJobId));
        assertTrue(queueContainsJob(secondJobId));
    }

    @Test
    @DisplayName("PO-2593 - Failed batch leaves no messages on the queue")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void publisherRollsBackTheCompleteBatchWhenALaterSendFails() throws Exception {
        // Arrange
        final long firstJobId = System.currentTimeMillis();
        final long secondJobId = firstJobId + 1;

        try (Connection realConnection = connectionFactory.createConnection()) {
            Session realSession = realConnection.createSession(true, Session.SESSION_TRANSACTED);
            Connection connection = spy(realConnection);
            Session session = spy(realSession);
            MessageProducer realProducer = realSession.createProducer(realSession.createQueue(queueName));
            MessageProducer producer = spy(realProducer);
            AtomicInteger sendCount = new AtomicInteger();

            doReturn(session).when(connection).createSession(anyBoolean(), anyInt());
            doReturn(producer).when(session).createProducer(any(Destination.class));
            doAnswer(invocation -> {
                if (sendCount.incrementAndGet() == 2) {
                    throw new JMSException("simulated broker send failure");
                }
                return invocation.callRealMethod();
            }).when(producer).send(any(Message.class));

            ConnectionFactory faultInjectingConnectionFactory = mock(ConnectionFactory.class);
            when(faultInjectingConnectionFactory.createConnection()).thenReturn(connection);

            JmsTemplate jmsTemplate = new JmsTemplate(faultInjectingConnectionFactory);
            jmsTemplate.setDeliveryPersistent(true);
            jmsTemplate.setExplicitQosEnabled(true);
            jmsTemplate.setSessionTransacted(true);

            InterfaceJobServiceBusProperties properties = new InterfaceJobServiceBusProperties();
            properties.setQueueName(queueName);

            // Act
            assertThrows(InterfaceJobQueueException.class, () ->
                new InterfaceJobQueuePublisherImpl(jmsTemplate, new ObjectMapper(), properties)
                    .publish(List.of(firstJobId, secondJobId)));
        }

        // Assert
        assertFalse(queueContainsJob(firstJobId));
        assertFalse(queueContainsJob(secondJobId));
    }

    private boolean queueContainsJob(long expectedJobId) {
        for (int attempt = 0; attempt < QUEUE_PEEK_ATTEMPTS; attempt++) {
            if (peekQueueForJob(expectedJobId)) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(QUEUE_PEEK_DELAY_MILLIS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while inspecting interface-job queue", exception);
            }
        }
        return false;
    }

    private boolean peekQueueForJob(long expectedJobId) {
        try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildClient()) {
            for (ServiceBusReceivedMessage message : receiver.peekMessages(MAX_PEEK_MESSAGES)) {
                if (messageContainsJob(message, expectedJobId)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to inspect interface-job queue", exception);
        }
    }

    private boolean messageContainsJob(ServiceBusReceivedMessage message, long expectedJobId) {
        try {
            JsonNode body = OBJECT_MAPPER.readTree(messageBody(message.getRawAmqpMessage()));
            return body.get("interface_job_id").asLong() == expectedJobId;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to parse interface-job queue message", exception);
        }
    }

    private String messageBody(AmqpAnnotatedMessage message) {
        var body = message.getBody();
        return switch (body.getBodyType()) {
            case VALUE -> String.valueOf(body.getValue());
            case DATA -> new String(body.getFirstData(), StandardCharsets.UTF_8);
            case SEQUENCE -> body.getSequence().toString();
        };
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
