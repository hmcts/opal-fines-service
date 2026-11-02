package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.TillQueueProperties;

/**
 * Publishes till allocation requests using the existing transactional JMS template.
 * The broker transaction is separate from the caller's database transaction.
 */
@Component
public class TillQueuePublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final TillQueueProperties properties;

    public TillQueuePublisher(@Qualifier("transactionalPublisherJmsTemplate") JmsTemplate jmsTemplate,
                              ObjectMapper objectMapper, TillQueueProperties properties) {

        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Publishes one JSON message per till, sharing a Service Bus session ID for ordered processing.
     */
    public void publish(List<Long> tillIds) {
        List<String> payloads = tillIds.stream().map(this::toPayload).toList();
        String sessionId = UUID.randomUUID().toString();
        jmsTemplate.execute(session -> sendBatch(session, payloads, sessionId), true);
    }

    private String toPayload(Long tillId) {
        try {
            return objectMapper.writeValueAsString(new TillQueueMessage(tillId));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialize till queue message", exception);
        }
    }

    /**
     * Commits the batch after all sends succeed, attempting rollback on failure.
     */
    private Void sendBatch(Session session, List<String> payloads, String sessionId) throws JMSException {
        try {
            try (MessageProducer producer = session.createProducer(session.createQueue(properties.getQueueName()))) {
                for (String payload : payloads) {
                    TextMessage message = session.createTextMessage(payload);
                    // Qpid maps JMSXGroupID to AMQP group-id, which Service Bus uses as the session ID.
                    message.setStringProperty("JMSXGroupID", sessionId);
                    producer.send(message);
                }
            }
            session.commit();
            return null;
        } catch (JMSException | RuntimeException exception) {
            try {
                session.rollback();
            } catch (JMSException rollbackException) {
                exception.addSuppressed(rollbackException);
            }
            throw exception;
        }
    }
}
