package uk.gov.hmcts.opal.service.refdata;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.SubQueue;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

//we need to be care receiving messages here while the main receiver is running.
//So don't think we can use a receiver to see if queue is empty.
//also consider this ui  image: ghcr.io/veselovandrey/servicebusviewer:latest

@Slf4j(topic = "opal.JsonFileTopicMessagePublisher")
@Service
public class EmulatorUtil implements AutoCloseable {

    private final ServiceBusSenderClient senderClient;
    private final ServiceBusReceiverClient queueReceiver;
    private final ServiceBusReceiverClient dlqReceiver;

    public static String CONNECTION_STRING =
        "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
    public static String TOPIC = "topic.refdata";
    public static String SUBSCRIPTION = "subscription.refdata";

    EmulatorUtil() {
        senderClient = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .sender()
            .topicName(TOPIC)
            .buildClient();
        queueReceiver = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .receiver()
            .topicName(TOPIC)
            .subscriptionName(SUBSCRIPTION)
            .buildClient();
        dlqReceiver = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
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
        queueReceiver.peekMessages(10).forEach(message -> {
            messages.add(message.getBody().toString());
        });
        return messages;
    }

    public List<String> peekDlqMessages() {
        List<String> messages = new ArrayList<>();
        dlqReceiver.peekMessages(10).forEach(message -> {
            messages.add(message.getBody().toString());
        });
        return messages;
    }

    @Override
    public void close() {
        senderClient.close();
        queueReceiver.close();
        dlqReceiver.close();
    }
}
