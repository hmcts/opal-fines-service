package uk.gov.hmcts.opal.service.refdata;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.cucumber.java.eo.Se;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "opal.JsonFileTopicMessagePublisher")
@Service
public class EmulatorUtil implements AutoCloseable {

    private final ServiceBusSenderClient senderClient;
    private final ServiceBusReceiverClient receiver;

    public static String CONNECTION_STRING =
        "Endpoint=sb://localhost:5672;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";
    public static String TOPIC = "topic.sr2";
    public static String SUBSCRIPTION = "subscription.1";

    EmulatorUtil() {
        senderClient = new ServiceBusClientBuilder()
            .connectionString("Endpoint=sb://localhost:5672;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;")
            .sender()
            .topicName("topic.sr2")
            .buildClient();

        //ServiceBusAdministrationClient doesn;t work with Java/Spring
        //The fact that getTotalMessageCount() is throwing an exception is because the local Azure Service Bus emulator does not implement the underlying metrics payload expected by the administration client. Under the hood, the emulator returns a simplified XML/JSON response to the management HTTP port, missing the nested data blocks the Java SDK looks for.
        receiver = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .receiver()
            .topicName(TOPIC)
            .subscriptionName(SUBSCRIPTION)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .buildClient();
    }

    public void publishConfiguredMessage(String messageString, String sessionId) {

        try {
            ServiceBusMessage message = new ServiceBusMessage(messageString);
            message.setSessionId("ref-data-session-id");
            senderClient.sendMessage(message);
            log.info(
                "Published JSON message from {} to topic {} with sessionId={}",
                "jsonFile",
                senderClient.getEntityPath(),
                sessionId
            );
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to publish JSON message to topic", ex);
        }
    }

    public List<ServiceBusReceivedMessage> getMessagesLeft() {
        //no messages on queue now
        // Attempt to read until it returns empty
        return receiver.receiveMessages(10, Duration.ofMillis(500)).stream().collect(Collectors.toList());
    }

    @Override
    public void close() {
        senderClient.close();
    }
}
