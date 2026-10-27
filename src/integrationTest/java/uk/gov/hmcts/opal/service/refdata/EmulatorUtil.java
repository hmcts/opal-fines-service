package uk.gov.hmcts.opal.service.refdata;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.SubQueue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "opal.JsonFileTopicMessagePublisher")
public class EmulatorUtil implements AutoCloseable {

    private final ServiceBusSenderClient senderClient;
    private final ServiceBusReceiverClient queueReceiver;
    private final ServiceBusReceiverClient dlqReceiver;

    private final String connectionString;

    public static final String TOPIC = "opal-reference-data-topic";
    public static final String SUBSCRIPTION = "opal-fines-reference-data-subscription";

    EmulatorUtil(String connectionString) {
        this.connectionString = connectionString;
        senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .topicName(TOPIC)
            .buildClient();
        queueReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .topicName(TOPIC)
            .subscriptionName(SUBSCRIPTION)
            .buildClient();
        dlqReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .topicName(TOPIC)
            .subscriptionName(SUBSCRIPTION)
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();
    }

    public void publishMessageToTopic(String messageString, String sessionId) {

        try {
            ServiceBusMessage message = new ServiceBusMessage(messageString);
            message.setSessionId(sessionId);
            senderClient.sendMessage(message);
            log.info("Published message to topic {}", TOPIC);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to publish JSON message to topic", ex);
        }
    }

    public List<String> peekQueueMessages() {
        List<String> messages = new ArrayList<>();
        queueReceiver.peekMessages(10, 0).forEach(message -> {
            messages.add(message.getBody().toString());
        });
        return messages;
    }

    public List<String> peekDlqMessages() {
        List<String> messages = new ArrayList<>();
        dlqReceiver.peekMessages(10, 0).forEach(message -> {
            messages.add(message.getBody().toString());
        });
        return messages;
    }

    public void clearSessionMessages(String sessionId) {
        if (peekQueueMessages().isEmpty()) {
            return;
        }
        try (var sessions = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .topicName(TOPIC)
            .subscriptionName(SUBSCRIPTION)
            .disableAutoComplete()
            .buildClient();
             var receiver = sessions.acceptSession(sessionId)) {
            receiver.receiveMessages(100, Duration.ofSeconds(2)).forEach(receiver::complete);
        }
    }

    @Override
    public void close() {
        senderClient.close();
        queueReceiver.close();
        dlqReceiver.close();
    }
}
