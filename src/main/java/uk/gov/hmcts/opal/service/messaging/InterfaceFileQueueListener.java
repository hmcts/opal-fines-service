package uk.gov.hmcts.opal.service.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;
import uk.gov.hmcts.opal.util.QueueUtil;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.InterfaceFileQueueListener")
@ConditionalOnExpression(
    "${opal.interface-file.service-bus.consumer-enabled} == true "
        + "and '${opal.automated-task:}' == ''"
)
public class InterfaceFileQueueListener {

    private final ObjectMapper objectMapper;

    private final InterfaceFileProcessorService interfaceFileProcessorService;

    @JmsListener(
        destination = "${opal.interface-file.service-bus.queue-name}",
        containerFactory = "opalFinesConsumerConnectionFactory"
    )
    public void onMessage(Message message) throws JMSException {
        consume(QueueUtil.getMessageAsString(message));
    }

    protected void consume(String messagePayload) {
        Long message = parse(messagePayload);

        log.info("Interface file queue message received. interfaceFileId={}", message);
        interfaceFileProcessorService.process(message);

    }

    //Suppressed as used to managed transactions / queue behaviour
    @SuppressWarnings("javabugs:S6416")
    private Long parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException(
                "Interface file message payload is blank"
            );
        }

        try {
            return objectMapper.readValue(
                messagePayload,
                Long.class
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
