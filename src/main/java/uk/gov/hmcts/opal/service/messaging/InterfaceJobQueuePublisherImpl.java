package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.InterfaceJobServiceBusProperties;
import uk.gov.hmcts.opal.exception.InterfaceJobQueueException;

@Component
@Slf4j(topic = "opal.InterfaceJobQueuePublisher")
public class InterfaceJobQueuePublisherImpl implements InterfaceJobQueuePublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final InterfaceJobServiceBusProperties properties;

    public InterfaceJobQueuePublisherImpl(@Qualifier("interfaceJobPublisherJmsTemplate") JmsTemplate jmsTemplate,
        ObjectMapper objectMapper, InterfaceJobServiceBusProperties properties) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void publish(List<Long> interfaceJobIds) {
        List<String> payloads = interfaceJobIds.stream()
            .map(this::toPayload)
            .toList();

        try {
            jmsTemplate.execute(session -> sendBatch(session, payloads), true);
            log.info("Published {} interface job messages", payloads.size());
        } catch (JmsException e) {
            throw new InterfaceJobQueueException("Unable to publish interface job messages", e);
        }
    }

    private String toPayload(Long interfaceJobId) {
        try {
            return objectMapper.writeValueAsString(new InterfaceJobQueueMessage(interfaceJobId));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to serialize interface job queue message", e);
        }
    }

    private Void sendBatch(Session session, List<String> payloads) {
        try {
            sendMessages(session, payloads);
            session.commit();
            return null;
        } catch (JMSException e) {
            rollback(session, e);
            throw new InterfaceJobQueueException("Unable to send interface job messages", e);
        }
    }

    private void sendMessages(Session session, List<String> payloads) throws JMSException {
        try (MessageProducer producer =
            session.createProducer(session.createQueue(properties.getQueueName()))) {
            for (String payload : payloads) {
                producer.send(session.createTextMessage(payload));
            }
        }
    }

    private void rollback(Session session, JMSException cause) {
        try {
            session.rollback();
        } catch (JMSException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
