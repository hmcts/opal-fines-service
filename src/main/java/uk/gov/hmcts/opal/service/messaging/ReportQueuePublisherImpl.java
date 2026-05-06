package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.config.ReportQueuePublisherProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@Slf4j
public class ReportQueuePublisherImpl implements ReportQueuePublisher {


    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final ReportQueuePublisherProperties properties;

    public ReportQueuePublisherImpl(@Qualifier("reportPublisherJmsTemplate") JmsTemplate jmsTemplate,
        ObjectMapper objectMapper, ReportQueuePublisherProperties properties) {
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
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize report queue message", ex);
        } catch (JmsException ex) {
            throw new IllegalStateException("Unable to publish report queue message", ex);
        }
    }
}
