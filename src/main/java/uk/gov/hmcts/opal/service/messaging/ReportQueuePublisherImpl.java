package uk.gov.hmcts.opal.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "opal.ReportPublishService")
public class ReportQueuePublisherImpl implements ReportQueuePublisher {
//    private final JmsTemplate jmsTemplate;
//    private final ObjectMapper objectMapper;
//    private final ReportServiceBusProperties properties;

    @Override
    public void publish(Long reportInstanceId) {

    }
}
