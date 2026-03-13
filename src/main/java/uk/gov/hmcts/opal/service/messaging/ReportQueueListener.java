package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportQueueListener")
@ConditionalOnProperty(prefix = "opal.report.consumer", name = "enabled", havingValue = "true")
public class ReportQueueListener {

    private final ReportQueueConsumerService consumer;

    @JmsListener(
        destination = "${opal.report.consumer.queue-name}",
        containerFactory = "reportListenerContainerFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getText();
            log.debug("Report queue message received payload={}", payload);
            consumer.consume(payload);
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }
}