package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSConsumer;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.TextMessage;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser.ConnectionDetails;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Real queue set up with the external Service Bus emulator.
 *
 * <p>
 * cd ../opal-shared-infrastructure
 * docker compose -f docker-compose-service-bus.yml up -d
 *
 * <p>
 * These tests use a queue-size baseline because there does not seem to be a reliable way of
 * draining the queue in {@code @BeforeEach}.
 */

@Disabled //Needs a real queue which is not currently available with our test container set-up
@TestPropertySource(properties = {
    "opal.interface-jobs.service-bus.consumer-enabled=false",
    "opal.report.service-bus.consumer-enabled=false"
})
@Sql(scripts = "classpath:db/insertData/insert_into_interface_job_queue_processing.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_job_queue_processing.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Interface Job Queue Service Bus Integration Tests")
class InterfaceJobRealQueueIntegrationTest extends AbstractIntegrationTest {

    private static final Long INTERFACE_JOB_ID = 99000000401000L;
    private static final String DEFAULT_CONNECTION_STRING =
        "Endpoint=sb://localhost/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;"
            + "UseDevelopmentEmulator=true";
    private static final String QUEUE_NAME = "auto-payments-process-interface-files";

    @Autowired
    private InterfaceJobQueueConsumerService interfaceJobQueueConsumerService;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceJobQueueIntegrationTestHelper helper;

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String reportContainerName;

    private JmsConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(reportContainerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }

        String serviceBusConnectionString = optionalEnv("SERVICEBUS_CONNECTION_STRING", DEFAULT_CONNECTION_STRING);
        String protocol = optionalEnv("SERVICEBUS_PROTOCOL", "amqp");
        ConnectionDetails details = new ServiceBusConnectionStringParser().parse(serviceBusConnectionString);

        String remoteUri = "%s://%s".formatted(protocol, details.fullyQualifiedNamespace());
        connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());
    }

    @Test
    @DisplayName("PO-2592 INT.03/INT.09 - Queue commit removes message")
    @JiraStory("PO-2592") // INT.03 / INT.09
    @JiraEpic("PO-2468")
    void completionRemovesMessageFromQueue() throws Exception {
        int beforeCount = queueMessageCount();

        sendInterfaceJobMessage();
        awaitQueueCount(beforeCount + 1);

        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            TextMessage message = receiveInterfaceJobMessage(context);
            interfaceJobQueueConsumerService.consume(message.getText());
            context.commit();
        }

        awaitQueueCount(beforeCount);
        assertThat(interfaceJobRepository.findById(INTERFACE_JOB_ID))
            .map(job -> job.getStatus())
            .contains(InterfaceJobStatus.COMPLETED);
        helper.assertSideEffects();
        assertThat(helper.findFailedInterfaceMessagesForJob()).isEmpty();
    }

    @Test
    @DisplayName("PO-2592 INT.09 - Queue rollback leaves message available for retry")
    @JiraStory("PO-2592") // INT.09
    @JiraEpic("PO-2468")
    void transientFailureLeavesMessageOnQueue() throws Exception {
        int beforeCount = queueMessageCount();

        blobServiceClient.getBlobContainerClient(reportContainerName).deleteIfExists();

        sendInterfaceJobMessage();
        awaitQueueCount(beforeCount + 1);

        try (JMSContext context = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
            TextMessage message = receiveInterfaceJobMessage(context);

            assertThatThrownBy(() -> interfaceJobQueueConsumerService.consume(message.getText()))
                .isInstanceOf(uk.gov.hmcts.opal.exception.ReportGenerationException.class);

            context.rollback();
        }

        awaitQueueCount(beforeCount + 1);
        helper.assertJobStatus(InterfaceJobStatus.PROCESSING, false);
        helper.assertNoSideEffects();
        assertThat(helper.findFailedInterfaceMessagesForJob()).isEmpty();
    }

    private void sendInterfaceJobMessage() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(QUEUE_NAME);
            context.createProducer().send(queue, "{\"interface_job_id\":" + INTERFACE_JOB_ID + "}");
        }
    }

    private TextMessage receiveInterfaceJobMessage(JMSContext context) {
        context.start();
        Queue queue = context.createQueue(QUEUE_NAME);
        JMSConsumer consumer = context.createConsumer(queue);
        Message message = consumer.receive(10_000);
        assertThat(message)
            .as("Expected a message on queue %s".formatted(QUEUE_NAME))
            .isInstanceOf(TextMessage.class);
        return (TextMessage) message;
    }

    private int queueMessageCount() {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(QUEUE_NAME);
            try (QueueBrowser browser = context.createBrowser(queue)) {
                Enumeration<?> messages = browser.getEnumeration();

                int count = 0;
                while (messages.hasMoreElements()) {
                    messages.nextElement();
                    count++;
                }
                return count;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to browse queue " + QUEUE_NAME, ex);
        }
    }

    private void awaitQueueCount(int expectedCount) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (queueMessageCount() == expectedCount) {
                return;
            }
            Thread.sleep(250);
        }

        assertThat(queueMessageCount())
            .as("Expected queue %s to contain %d messages".formatted(QUEUE_NAME, expectedCount))
            .isEqualTo(expectedCount);
    }

    private static String optionalEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
