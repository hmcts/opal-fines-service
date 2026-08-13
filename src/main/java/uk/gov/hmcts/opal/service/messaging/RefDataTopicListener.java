package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
public class RefDataTopicListener {

    private final RefDataMessageProcessor consumer;

    @JmsListener(
        destination = "${opal.ref-data.service-bus.topic-name}",
        subscription = "${opal.ref-data.service-bus.subscription-name}",
        containerFactory = "refDataTopicListenerContainerFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            consumer.processMessage(textMessage.getText());
        } else {
            throw new IllegalArgumentException("Message must be of type TextMessage");
        }
    }
}
