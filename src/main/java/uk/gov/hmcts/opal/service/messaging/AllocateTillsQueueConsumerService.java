package uk.gov.hmcts.opal.service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.opal.till.AllocateTillsStatusService;

@Service
@Slf4j(topic = "opal.AllocateTillsQueueConsumer")
@RequiredArgsConstructor
public class AllocateTillsQueueConsumerService implements QueueConsumerInterface {

    private final ObjectMapper objectMapper;
    private final AllocateTillsStatusService statusService;
    private final AllocateTillsQueueProcessingService processingService;
    private final TransientFailureHelper transientFailureHelper;

    @Override
    public void consume(String messagePayload) {
        TillQueueMessage message = parse(messagePayload);
        if (message == null || message.tillId() == null || message.tillId() <= 0) {
            throw new IllegalArgumentException("Till queue message must contain a positive till_id");
        }
        if (!statusService.isProcessing(message.tillId())) {
            throw new TillNotProcessingException(message.tillId());
        }
        try {
            processingService.process(message);
        } catch (RuntimeException exception) {
            if (transientFailureHelper.isTransientTillAllocationFailure(exception)) {
                throw exception;
            }
            log.error("Till {} allocation failed non-transiently; marking Failed. Failure type: {}",
                message.tillId(), exception.getClass().getSimpleName());
            statusService.markFailed(message.tillId());
        }
    }

    private TillQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("Till queue message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, TillQueueMessage.class);
        } catch (JacksonException exception) {
            log.error("Till queue message parse failed", exception);
            throw new IllegalArgumentException("Unable to parse till queue message payload", exception);
        }
    }
}
