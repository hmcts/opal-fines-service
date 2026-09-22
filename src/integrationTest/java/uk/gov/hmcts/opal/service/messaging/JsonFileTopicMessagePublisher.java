package uk.gov.hmcts.opal.service.messaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j(topic = "opal.JsonFileTopicMessagePublisher")
public class JsonFileTopicMessagePublisher implements AutoCloseable {

    private static final Path CONFIG_FILE = Path.of(".local/ref-data-publisher.properties");

    private static final String CONNECTION_STRING_PROPERTY = "connection-string";
    private static final String TOPIC_NAME_PROPERTY = "topic-name";
    private static final String SUBSCRIPTION_NAME_PROPERTY = "subscription-name";
    private static final String JSON_FILE_PROPERTY = "json-file";
    private static final String SESSION_ID_PROPERTY = "session-id";

    private static final Path DEFAULT_JSON_FILE = Path.of("src/test/resources/test_payloads/lja_message.json");
    private static final String DEFAULT_SESSION_ID = "refdata-session";

    private final ServiceBusSenderClient senderClient;
    private final Path jsonFile;
    private final String sessionId;

    public JsonFileTopicMessagePublisher() {
        this(loadConfig());
    }

    JsonFileTopicMessagePublisher(ServiceBusSenderClient senderClient) {
        this(senderClient, DEFAULT_JSON_FILE, DEFAULT_SESSION_ID);
    }

    JsonFileTopicMessagePublisher(PublisherConfig config) {
        this(buildSenderClient(config), config.jsonFile(), config.sessionId());
    }

    private JsonFileTopicMessagePublisher(ServiceBusSenderClient senderClient, Path jsonFile, String sessionId) {
        this.senderClient = senderClient;
        this.jsonFile = jsonFile;
        this.sessionId = sessionId;
    }

    public static void main(String[] args) {
        try (JsonFileTopicMessagePublisher publisher = new JsonFileTopicMessagePublisher()) {
            publisher.publishConfiguredMessage();
        }
    }

    public void publishConfiguredMessage() {
        publish(jsonFile, sessionId);
    }

    public void publish(Path jsonFile, String sessionId) {
        validateInputs(jsonFile, sessionId);
        String payload = readPayload(jsonFile);

        try {
            ServiceBusMessage message = new ServiceBusMessage(payload);
            message.setSessionId(sessionId);
            senderClient.sendMessage(message);
            log.info(
                "Published JSON message from {} to topic {} with sessionId={}",
                jsonFile,
                senderClient.getEntityPath(),
                sessionId
            );
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to publish JSON message to topic", ex);
        }
    }

    private static ServiceBusSenderClient buildSenderClient(PublisherConfig config) {
        return new ServiceBusClientBuilder()
            .connectionString(config.connectionString())
            .sender()
            .topicName(config.topicName())
            .buildClient();
    }

    static PublisherConfig loadConfig() {
        Properties properties = new Properties();
        try (var inputStream = Files.newInputStream(CONFIG_FILE)) {
            properties.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load ref-data publisher config from " + CONFIG_FILE, ex);
        }

        String connectionString = requiredProperty(properties, CONNECTION_STRING_PROPERTY);
        String topicName = requiredProperty(properties, TOPIC_NAME_PROPERTY);
        String subscriptionName = requiredProperty(properties, SUBSCRIPTION_NAME_PROPERTY);
        Path jsonFile = Path.of(optionalProperty(properties, JSON_FILE_PROPERTY, DEFAULT_JSON_FILE.toString()));
        String sessionId = optionalProperty(properties, SESSION_ID_PROPERTY, DEFAULT_SESSION_ID);

        return new PublisherConfig(connectionString, topicName, subscriptionName, jsonFile, sessionId);
    }

    private static String requiredProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Missing required property '%s' in %s".formatted(name, CONFIG_FILE));
        }
        return value.trim();
    }

    private static String optionalProperty(Properties properties, String name, String defaultValue) {
        String value = properties.getProperty(name);
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private static void validateInputs(Path jsonFile, String sessionId) {
        if (jsonFile == null) {
            throw new IllegalArgumentException("jsonFile is required");
        }
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException("sessionId is required");
        }
    }

    private static String readPayload(Path jsonFile) {
        try {
            String payload = Files.readString(jsonFile, StandardCharsets.UTF_8);
            if (!StringUtils.hasText(payload)) {
                throw new IllegalArgumentException("JSON message file is blank: " + jsonFile);
            }
            return payload;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read JSON message file: " + jsonFile, ex);
        }
    }

    @Override
    public void close() {
        senderClient.close();
    }

    record PublisherConfig(String connectionString, String topicName, String subscriptionName, Path jsonFile,
                           String sessionId) {
    }
}
