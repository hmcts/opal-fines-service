package uk.gov.hmcts.opal.utils;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Peeks the auto-payments-process-interface-files queue for PO-2593 functional assertions.
 */
public final class InterfaceJobsProcessQueueClient {

    private static final String SERVICE_BUS_CONNECTION_STRING = "SERVICEBUS_CONNECTION_STRING";
    private static final String SERVICE_BUS_QUEUE = "SERVICEBUS_INTERFACE_JOBS_QUEUE_NAME";
    private static final int MAX_PEEK_MESSAGES = 100;
    private static final int QUEUE_PEEK_ATTEMPTS = 10;
    private static final long QUEUE_PEEK_DELAY_MILLIS = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Waits briefly for a message containing the requested interface job ID.
     *
     * @param expectedJobId interface job identifier to find.
     * @return true when a matching message is found before the retry limit.
     */
    public boolean eventuallyContainsJob(long expectedJobId) {
        for (int attempt = 0; attempt < QUEUE_PEEK_ATTEMPTS; attempt++) {
            if (peekQueueForJob(expectedJobId)) {
                return true;
            }
            if (attempt < QUEUE_PEEK_ATTEMPTS - 1) {
                sleepBeforeRetry();
            }
        }
        return false;
    }

    private boolean peekQueueForJob(long expectedJobId) {
        try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(requiredEnvironmentVariable(SERVICE_BUS_CONNECTION_STRING))
            .receiver()
            .queueName(System.getenv().getOrDefault(SERVICE_BUS_QUEUE, "auto-payments-process-interface-files"))
            .buildClient()) {
            for (ServiceBusReceivedMessage message : receiver.peekMessages(MAX_PEEK_MESSAGES)) {
                if (messageHasJob(message, expectedJobId)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to inspect the auto-payments-process-interface-files queue",
                exception);
        }
    }

    private boolean messageHasJob(ServiceBusReceivedMessage message, long expectedJobId) {
        try {
            JsonNode body = objectMapper.readTree(messageBody(message.getRawAmqpMessage()));
            return body.path("interface_job_id").asLong(Long.MIN_VALUE) == expectedJobId;
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to parse an interface-job queue message", exception);
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

    private void sleepBeforeRetry() {
        try {
            TimeUnit.MILLISECONDS.sleep(QUEUE_PEEK_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while inspecting the interface-job queue", exception);
        }
    }

    private String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " before running PO-2593 functional tests");
        }
        return value;
    }
}
