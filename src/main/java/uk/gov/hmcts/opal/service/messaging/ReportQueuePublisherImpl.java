package uk.gov.hmcts.opal.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.config.ReportServiceBusProperties;

@Component
@Slf4j(topic = "opal.ReportPublishService")
public class ReportQueuePublisherImpl implements ReportQueuePublisher {
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final ReportServiceBusProperties properties;

    public ReportQueuePublisherImpl(@Qualifier("reportPublisherJmsTemplate") JmsTemplate jmsTemplate,
        ObjectMapper objectMapper, ReportServiceBusProperties properties) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void publish(Long reportInstanceId) {
        try {
            String payload = objectMapper.writeValueAsString(new ReportQueueMessage(reportInstanceId));
            jmsTemplate.convertAndSend(properties.getQueueName(), payload);
            log.info("Published report queue message for reportInstanceId={}", reportInstanceId);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to serialize report queue message", ex);
        } catch (JmsException ex) {
            throw new IllegalStateException("Unable to publish report queue message", ex);
        }
    }
}
