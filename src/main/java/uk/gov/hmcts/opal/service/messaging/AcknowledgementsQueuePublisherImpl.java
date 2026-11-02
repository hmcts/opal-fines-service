package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.AcknowledgementsServiceBusProperties;
import uk.gov.hmcts.opal.service.messaging.AcknowledgementsQueueMessage.Status;

@Component
@Slf4j(topic = "opal.AcknowledgementsQueuePublisher")
public class AcknowledgementsQueuePublisherImpl implements AcknowledgementsQueuePublisher{
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final AcknowledgementsServiceBusProperties properties;

    public AcknowledgementsQueuePublisherImpl(
        @Qualifier("acknowledgementsPublisherJmsTemplate") JmsTemplate jmsTemplate,
        ObjectMapper objectMapper, AcknowledgementsServiceBusProperties properties) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void publish(long vpfpoRegistrationId, long defendantId) {
        publishMessage(
            new AcknowledgementsQueueMessage(vpfpoRegistrationId, defendantId, Status.PUBLISHED)
        );
        log.info("Published acknowledgements queue message for vpfpoRegistrationId={}", vpfpoRegistrationId);
    }

    public void publishError(long vpfpoRegistrationId) {
        publishMessage(
            new AcknowledgementsQueueMessage(vpfpoRegistrationId, null, Status.PUBLISHING_FAILED)
        );
        log.info("Published acknowledgements queue message error for vpfpoRegistrationId={}", vpfpoRegistrationId);
    }

    private void publishMessage(AcknowledgementsQueueMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            jmsTemplate.execute(session -> sendMessage(session, payload), true);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to serialize report queue message", ex);
        } catch (JmsException ex) {
            throw new IllegalStateException("Unable to publish report queue message", ex);
        }
    }

    private Void sendMessage(Session session, String payload) throws JMSException {
        try (MessageProducer producer = session.createProducer(session.createQueue(properties.getQueueName()))) {
            producer.send(session.createTextMessage(payload));
            session.commit();
            return null;
        }
        catch (JMSException e) {
            rollback(session, e);
            throw e;
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
