package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.InterfaceFileQueueListener")
@ConditionalOnExpression(
    "${opal.interface-file.service-bus.consumer-enabled} == true "
        + "and '${opal.automated-task:}' == ''"
)
public class InterfaceFileQueueListener {

    private final ObjectMapper objectMapper;

    // TODO PO-6460 placeholder
    private final InterfaceFileProcessorService interfaceFileProcessorService;

    @JmsListener(
        destination = "${opal.interface-file.service-bus.queue-name}",
        containerFactory = "interfaceFileListenerContainerFactory"
    )
    public void onMessage(Message message) throws JMSException {
        if (message instanceof TextMessage textMessage) {
            consume(textMessage.getText());
        } else {
            throw new IllegalArgumentException(
                "Message must be of type TextMessage"
            );
        }
    }

    protected void consume(String messagePayload) {
        InterfaceFileQueueMessage message = parse(messagePayload);

        // TODO PO-6460 placeholder
        interfaceFileProcessorService.process(message.interfaceFileId());

        log.info(
            "Interface file queue message received. interfaceFileId={}",
            message.interfaceFileId()
        );
    }

    private InterfaceFileQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException(
                "Interface file message payload is blank"
            );
        }

        try {
            return objectMapper.readValue(
                messagePayload,
                InterfaceFileQueueMessage.class
            );
        } catch (JacksonException ex) {
            log.error("Interface file queue message parse failed", ex);
            throw new IllegalArgumentException(
                "Unable to parse Interface file message payload",
                ex
            );
        }
    }
}
