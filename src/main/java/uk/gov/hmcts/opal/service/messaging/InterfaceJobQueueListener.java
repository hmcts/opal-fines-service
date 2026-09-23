package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.InterfaceJobQueueListener")
@ConditionalOnExpression("${opal.interface-jobs.service-bus.consumer-enabled} == true "
    + "and '${opal.automated-task:}' == ''")
public class InterfaceJobQueueListener {

    private final InterfaceJobQueueConsumerService consumer;

    @JmsListener(
        destination = "${opal.interface-jobs.service-bus.queue-name}",
        containerFactory = "interfaceJobListenerContainerFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            consumer.consume(textMessage.getText());
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }
}
