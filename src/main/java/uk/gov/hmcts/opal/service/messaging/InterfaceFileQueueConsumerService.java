package uk.gov.hmcts.opal.service.messaging;

import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.InterfaceFileProcessorService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.InterfaceFileQueueConsumer")
public class InterfaceFileQueueConsumerService implements QueueConsumerInterface {

    private final ObjectMapper objectMapper;

    // TODO PO-6460 placeholder
    private final InterfaceFileProcessorService interfaceFileProcessorService;

    @Override
    public void consume(String messagePayload) {
        InterfaceFileQueueMessage message = parse(messagePayload);
        // TODO PO-6460 placeholder
        interfaceFileProcessorService.process(message.interfaceFileId());
        log.info("Interface file queue message received. interfaceFileId={}", message.interfaceFileId());
    }

    private InterfaceFileQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("Interface file message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, InterfaceFileQueueMessage.class);
        } catch (JacksonException ex) {
            log.error("Interface file queue message parse failed", ex);
            throw new IllegalArgumentException("Unable to parse Interface file message payload", ex);
        }
    }
}
