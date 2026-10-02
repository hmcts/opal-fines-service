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
@Slf4j(topic = "opal.InterfaceFileQueueListener")
@ConditionalOnExpression(
    "${opal.interface-file.service-bus.consumer-enabled} == true "
        + "and '${opal.automated-task:}' == ''"
)
public class InterfaceFileQueueListener {

    private final InterfaceFileQueueConsumerService consumer;

    @JmsListener(
        destination = "${opal.interface-file.service-bus.queue-name}",
        containerFactory = "interfaceFileListenerContainerFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getText();
            consumer.consume(payload);
        } else {
            throw new IllegalArgumentException(
                "Message must be of type TextMessage"
            );
        }
    }
}
