package uk.gov.hmcts.opal.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "opal.ReportPublishService")
public class ReportQueuePublisherImpl implements ReportQueuePublisher {

    @Override
    public void publish(Long reportInstanceId) {

    }
}
