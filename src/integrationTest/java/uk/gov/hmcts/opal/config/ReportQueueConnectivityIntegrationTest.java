package uk.gov.hmcts.opal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.TextMessage;
import java.util.Enumeration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.service.messaging.ReportQueueMessage;

/**
 * Manual helper that publishes a report message to a queue for developer testing.
 *
 * - Before using this test, set the environment variable REPORT_QUEUE_ASB_TEST_ENABLED=true
 * - Ensure that you have run the docker scripts in `opal-shared-infrastructure and that Azure Service Bus and
 *   Azurite are running in docker
 * - To check that Blob files have been saved to Blob storage you can install Microsoft Azure Storage Explorer and
 *   connect it to your local running Azurite instance.
 * - To view messages on the queue use peekAtQueue()
 */
@EnabledIfEnvironmentVariable(named = "REPORT_QUEUE_ASB_TEST_ENABLED", matches = "true")
class ReportQueueConnectivityIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportQueueConnectivityIntegrationTest.class);

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private String queueName;

    private JmsConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        String connectionString = optionalEnv("SERVICEBUS_CONNECTION_STRING",
            "Endpoint=sb://localhost/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;"
                + "UseDevelopmentEmulator=true");
        queueName = optionalEnv("SERVICEBUS_REPORT_QUEUE_NAME", "report");
        String protocol = optionalEnv("SERVICEBUS_REPORT_PROTOCOL", "amqp");

        ServiceBusConnectionStringParser.ConnectionDetails details =
            ServiceBusConnectionStringParser.parse(connectionString);

        String remoteUri = "%s://%s".formatted(protocol, details.fullyQualifiedNamespace());
        connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());
    }

    @Test
    void sendsReportMessageToQueue() throws Exception {
        long instanceId = 99000000008000L;

        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(queueName);
            context.createProducer().send(queue, objectMapper.writeValueAsString(new ReportQueueMessage(instanceId)));

            LOGGER.info("Sent Report test message with instanceId={}", instanceId);
        }
    }

    @Test
    void peekAtQueue() throws Exception {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(queueName);
            QueueBrowser browser = context.createBrowser(queue);
            Enumeration<?> messages = browser.getEnumeration();

            if (messages.hasMoreElements()) {
                Message message = (Message) messages.nextElement();

                if (message instanceof TextMessage textMessage) {
                    System.out.println("Peeked message: " + textMessage.getText());
                }
            } else {
                System.out.println("Queue is empty");
            }
        }
    }

    private static String optionalEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
