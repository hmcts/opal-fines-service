package uk.gov.hmcts.opal.service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobStatusService;

@Service
@Slf4j(topic = "opal.InterfaceJobQueueConsumer")
@RequiredArgsConstructor
public class InterfaceJobQueueConsumerService implements QueueConsumerInterface {

    private final ObjectMapper objectMapper;
    private final InterfaceJobStatusService interfaceJobStatusService;
    private final InterfaceJobQueueProcessingService interfaceJobQueueProcessingService;
    private final TransientFailureHelper transientFailureHelper;

    @Override
    public void consume(String messagePayload) {
        InterfaceJobQueueMessage message = parse(messagePayload);
        Long interfaceJobId = message.interfaceJobId();
        if (!interfaceJobStatusService.isProcessing(interfaceJobId)) {
            log.error("Interface job {} is not PROCESSING, rejecting message", interfaceJobId);
            throw new IllegalStateException("Interface job " + interfaceJobId + " is not PROCESSING");
        }
        try {
            interfaceJobQueueProcessingService.processProcessingJob(interfaceJobId);
        } catch (RuntimeException ex) {
            if (transientFailureHelper.isTransientFailure(ex)) {
                throw ex;
            }
            log.error("Interface job {} failed non-transiently, marking failed", interfaceJobId, ex);
            try {
                interfaceJobQueueProcessingService.handleProcessingFailure(interfaceJobId, ex);
            } catch (RuntimeException persistenceException) {
                log.error("Unable to persist non-transient failure for interface job {}", interfaceJobId,
                    persistenceException);
            }
        }
    }

    private InterfaceJobQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("Interface job message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, InterfaceJobQueueMessage.class);
        } catch (JacksonException ex) {
            log.error("Interface job message parse failed", ex);
            throw new IllegalArgumentException("Unable to parse interface job message payload", ex);
        }
    }
}
