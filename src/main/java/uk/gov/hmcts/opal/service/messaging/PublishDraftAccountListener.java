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
@Slf4j(topic = "opal.PublishDraftAccountListener")
@ConditionalOnExpression("${opal.publish-draft-accounts.service-bus.consumer-enabled} == true "
    + "and '${opal.automated-task:}' == ''")
public class PublishDraftAccountListener {

    private final PublishDraftAccountConsumerService consumer;

    @JmsListener(
        destination = "${opal.publish-draft-accounts.service-bus.queue-name}",
        containerFactory = "opalFinesConsumerConnectionFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getText();
            consumer.consume(payload);
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }
}
