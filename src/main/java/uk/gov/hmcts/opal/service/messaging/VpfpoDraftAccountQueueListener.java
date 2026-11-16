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
@Slf4j(topic = "opal.VpfpoDraftAccountQueueListener")
@RequiredArgsConstructor
@ConditionalOnExpression(
    "${opal.draft-accounts.create.service-bus.consumer-enabled:false} and '${opal.automated-task:}' == ''")
public class VpfpoDraftAccountQueueListener {

    private final VpfpoDraftAccountConsumerService consumer;

    @JmsListener(destination =  "${opal.draft-accounts.create.service-bus.queue-name}",
        containerFactory = "opalFinesConsumerConnectionFactory")
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            consumer.consume(textMessage.getText());
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }

}
