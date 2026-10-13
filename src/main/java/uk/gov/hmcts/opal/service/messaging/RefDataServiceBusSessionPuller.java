package uk.gov.hmcts.opal.service.messaging;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataMessageProcessor;

@Slf4j(topic = "opal.RefDataServiceBusSessionPuller")
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.ref-data.service-bus", name = "consumer-enabled", havingValue = "true")
public class RefDataServiceBusSessionPuller {

    private static final int MAX_MESSAGES_PER_SESSION = 10;
    private static final long POLL_RATE_MILLISECONDS = 300_000L;
    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(3);

    private final ServiceBusSessionReceiverClient sessionReceiverClient;
    private final RefDataMessageProcessor processor;

    @Scheduled(initialDelay = POLL_RATE_MILLISECONDS, fixedDelay = POLL_RATE_MILLISECONDS)
    public void pullSessionMessages() {
        try {
            Optional<ServiceBusReceiverClient> sessionReceiver = acceptNextSession();
            if (sessionReceiver.isEmpty()) {
                return;
            }
            try (ServiceBusReceiverClient sessionWorker = sessionReceiver.get()) {
                processSessionMessages(sessionWorker);
            }
        } catch (RuntimeException ex) {
            log.warn("Unable to pull ref-data Service Bus session messages", ex);
        }
    }

    private Optional<ServiceBusReceiverClient> acceptNextSession() {
        try {
            return Optional.of(sessionReceiverClient.acceptNextSession());
        } catch (ServiceBusException ex) {
            if (isNoAvailableSession(ex)) {
                log.debug("No active ref-data Service Bus message sessions found");
                return Optional.empty();
            }
            throw ex;
        }
    }

    private void processSessionMessages(ServiceBusReceiverClient sessionWorker) {
        String sessionId = sessionWorker.getSessionId();
        log.info("Acquired ref-data Service Bus session lock. sessionId={}", sessionId);

        IterableStream<ServiceBusReceivedMessage> messages = sessionWorker.receiveMessages(
            MAX_MESSAGES_PER_SESSION,
            RECEIVE_TIMEOUT
        );

        for (ServiceBusReceivedMessage message : messages) {
            processMessage(sessionWorker, message);
        }
    }

    private void processMessage(ServiceBusReceiverClient sessionWorker, ServiceBusReceivedMessage message) {
        try {
            processor.processMessage(message.getBody().toString());
            sessionWorker.complete(message);
        } catch (RuntimeException ex) {
            abandonMessage(sessionWorker, message, ex);
            throw ex;
        }
    }

    private void abandonMessage(
        ServiceBusReceiverClient sessionWorker, ServiceBusReceivedMessage message, RuntimeException ex) {

        log.warn(
            "Unable to process ref-data Service Bus message. messageId={}, sessionId={}",
            message.getMessageId(),
            message.getSessionId(),
            ex
        );
        try {
            sessionWorker.abandon(message);
        } catch (RuntimeException abandonException) {
            ex.addSuppressed(abandonException);
        }
    }

    private boolean isNoAvailableSession(ServiceBusException ex) {
        return ServiceBusFailureReason.SERVICE_TIMEOUT.equals(ex.getReason())
            || ServiceBusFailureReason.SESSION_CANNOT_BE_LOCKED.equals(ex.getReason());
    }
}
