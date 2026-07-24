package uk.gov.hmcts.opal.service.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser.ConnectionDetails;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Run the shared Service Bus emulator locally.
 * cd ../opal-shared-infrastructure
 * docker compose -f docker-compose-service-bus.yml up -d
 */

@EnabledIfEnvironmentVariable(named = "INTERFACE_JOB_QUEUE_ASB_TEST_ENABLED", matches = "true")
@TestPropertySource(properties = "opal.interface-jobs.service-bus.consumer-enabled=true")
@DisplayName("Interface Job Queue Real Queue Integration Tests")
class InterfaceJobQueueRealQueueIntegrationTest extends AbstractInterfaceJobQueueProcessingIntegrationTest {

    private static final Long INTERFACE_JOB_ID = 99000000401000L;
    private static final BigDecimal EXPECTED_PAYMENT_AMOUNT = new BigDecimal("123.45");
    private static final String SERVICE_BUS_CONNECTION_STRING =
        "Endpoint=sb://localhost/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=local;"
            + "UseDevelopmentEmulator=true";
    private static final long QUEUE_PROCESSING_TIMEOUT_MS = 30_000L;
    private static final long QUEUE_POLL_INTERVAL_MS = 250L;
    public static final String INTERFACE_FILES_QUEUE = "auto-payments-process-interface-files";

    @Autowired
    private InterfaceJobQueueIntegrationTestHelper interfaceJobQueueHelper;

    @Test
    @JiraStory("INT.03")
    @JiraEpic("INT.03")
    void int03ValidProcessingMessageCompletesAndDrainsQueue() throws InterruptedException {
        sendQueueMessage(interfaceJobQueueHelper.validProcessingMessage());
        awaitEventually(() -> {
            InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(INTERFACE_JOB_ID)
                .orElseThrow();
            assertThat(interfaceJob.getStatus()).isEqualTo(InterfaceJobStatus.COMPLETED);
            assertThat(interfaceJob.getCompletedDateTime()).isNotNull();
            assertThat(interfaceJobQueueHelper.countTillsCreatedForInterfaceFile()).isEqualTo(1);
            assertThat(interfaceJobQueueHelper.countPaymentsCreatedForDefendantAccount()).isEqualTo(1);
            assertThat(interfaceJobQueueHelper.findPaymentAmountForDefendantAccount())
                .isEqualByComparingTo(EXPECTED_PAYMENT_AMOUNT);
            assertThat(interfaceJobQueueHelper.countPreAllocatedCashTillReports()).isEqualTo(1);
            assertThat(interfaceJobQueueHelper.findPreAllocatedCashTillReports())
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getGenerationStatus()).isEqualTo(ReportInstanceGenerationStatus.READY);
                    assertThat(report.getLocation()).isNotBlank();
                    assertThat(report.getErrors()).isNull();
                    assertThat(reportBlobExists(report.getLocation())).isTrue();
                });
        });
        awaitEventually(() -> assertThat(queueMessageCount()).isZero());
    }

    private void sendQueueMessage(String payload) {
        String connectionString = optionalEnv("SERVICEBUS_CONNECTION_STRING", SERVICE_BUS_CONNECTION_STRING);
        String queueName = optionalEnv("SERVICEBUS_INTERFACE_JOBS_QUEUE_NAME", INTERFACE_FILES_QUEUE);
        String protocol = optionalEnv("SERVICEBUS_PROTOCOL", "amqp");

        ConnectionDetails details = new ServiceBusConnectionStringParser().parse(connectionString);
        String remoteUri = "%s://%s".formatted(protocol, details.fullyQualifiedNamespace());
        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());

        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(queueName);
            context.createProducer().send(queue, payload);
        }
    }

    private int queueMessageCount() {
        String connectionString = optionalEnv("SERVICEBUS_CONNECTION_STRING", SERVICE_BUS_CONNECTION_STRING);
        String queueName = optionalEnv("SERVICEBUS_INTERFACE_JOBS_QUEUE_NAME", INTERFACE_FILES_QUEUE);
        String protocol = optionalEnv("SERVICEBUS_PROTOCOL", "amqp");

        ConnectionDetails details = new ServiceBusConnectionStringParser().parse(connectionString);
        String remoteUri = "%s://%s".formatted(protocol, details.fullyQualifiedNamespace());
        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());

        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue queue = context.createQueue(queueName);
            QueueBrowser browser = context.createBrowser(queue);
            Enumeration<?> messages = browser.getEnumeration();
            int count = 0;
            while (messages.hasMoreElements()) {
                Message message = (Message) messages.nextElement();
                if (message != null) {
                    count++;
                }
            }
            return count;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to inspect " + queueName + " queue", ex);
        }
    }

    private void awaitEventually(ThrowingRunnable assertions) throws InterruptedException {
        long deadline = System.currentTimeMillis() + QUEUE_PROCESSING_TIMEOUT_MS;
        AssertionError lastFailure = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                assertions.run();
                return;
            } catch (AssertionError ex) {
                lastFailure = ex;
                TimeUnit.MILLISECONDS.sleep(QUEUE_POLL_INTERVAL_MS);
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
        }
    }

    private static String optionalEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws InterruptedException;
    }
}
