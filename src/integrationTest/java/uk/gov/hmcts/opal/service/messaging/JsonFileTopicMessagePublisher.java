package uk.gov.hmcts.opal.service.messaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.opal.config.ServiceBusConnectionStringParser;

@Slf4j(topic = "opal.JsonFileTopicMessagePublisher")
public class JsonFileTopicMessagePublisher {

    private static final Path CONFIG_FILE = Path.of(".local/ref-data-publisher.properties");

    private static final String CONNECTION_STRING_PROPERTY = "connection-string";
    private static final String TOPIC_NAME_PROPERTY = "topic-name";
    private static final String SUBSCRIPTION_NAME_PROPERTY = "subscription-name";
    private static final String JSON_FILE_PROPERTY = "json-file";
    private static final String SESSION_ID_PROPERTY = "session-id";
    private static final String PROTOCOL_PROPERTY = "protocol";

    private static final String DEFAULT_PROTOCOL = "amqps";
    private static final long IDLE_TIMEOUT_MS = 30000;
    private static final long SEND_TIMEOUT_MS = 10000;
    private static final Path DEFAULT_JSON_FILE = Path.of("src/test/resources/test_payloads/lja_message.json");
    private static final String DEFAULT_SESSION_ID = "refdata-session";
    private static final String SERVICE_BUS_SESSION_ID_PROPERTY = "JMSXGroupID";

    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final Path jsonFile;
    private final String sessionId;

    public JsonFileTopicMessagePublisher() {
        this(loadConfig());
    }

    JsonFileTopicMessagePublisher(ConnectionFactory connectionFactory, String topicName) {
        this(connectionFactory, topicName, DEFAULT_JSON_FILE, DEFAULT_SESSION_ID);
    }

    JsonFileTopicMessagePublisher(PublisherConfig config) {
        this(buildConnectionFactory(config), config.topicName(), config.jsonFile(), config.sessionId());
    }

    private JsonFileTopicMessagePublisher(ConnectionFactory connectionFactory, String topicName, Path jsonFile,
                                          String sessionId) {
        this.connectionFactory = connectionFactory;
        this.topicName = topicName;
        this.jsonFile = jsonFile;
        this.sessionId = sessionId;
    }

    public static void main(String[] args) {
        new JsonFileTopicMessagePublisher().publishConfiguredMessage();
    }

    public void publishConfiguredMessage() {
        publish(jsonFile, sessionId);
    }

    public void publish(Path jsonFile, String sessionId) {
        validateInputs(jsonFile, sessionId);
        String payload = readPayload(jsonFile);

        try (JMSContext context = connectionFactory.createContext()) {
            Topic topic = context.createTopic(topicName);
            TextMessage message = context.createTextMessage(payload);
            message.setStringProperty(SERVICE_BUS_SESSION_ID_PROPERTY, sessionId);
            JMSProducer producer = context.createProducer();
            producer.send(topic, message);
            log.info("Published JSON message from {} to topic {} with sessionId={}", jsonFile, topicName, sessionId);
        } catch (JMSException | JMSRuntimeException ex) {
            throw new IllegalStateException("Unable to publish JSON message to topic", ex);
        }
    }

    static String buildRemoteUri(String fullyQualifiedNamespace, String protocol) {
        return "%s://%s?jms.sendTimeout=%d&amqp.idleTimeout=%d".formatted(
            protocol,
            fullyQualifiedNamespace,
            SEND_TIMEOUT_MS,
            IDLE_TIMEOUT_MS
        );
    }

    private static ConnectionFactory buildConnectionFactory(PublisherConfig config) {
        ServiceBusConnectionStringParser.ConnectionDetails details =
            new ServiceBusConnectionStringParser().parse(config.connectionString());
        String remoteUri = buildRemoteUri(details.fullyQualifiedNamespace(), config.protocol());

        JmsConnectionFactory connectionFactory = new JmsConnectionFactory(remoteUri);
        connectionFactory.setUsername(details.sharedAccessKeyName());
        connectionFactory.setPassword(details.sharedAccessKey());
        return connectionFactory;
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
        String protocol = optionalProperty(properties, PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);

        return new PublisherConfig(connectionString, topicName, subscriptionName, jsonFile, sessionId, protocol);
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

    record PublisherConfig(String connectionString, String topicName, String subscriptionName, Path jsonFile,
                           String sessionId,
                           String protocol) {
    }
}
