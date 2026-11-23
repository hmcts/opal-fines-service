package uk.gov.hmcts.opal.support;

import java.util.ArrayDeque;
import java.util.Queue;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.messaging.AcknowledgementsQueueMessage;
import uk.gov.hmcts.opal.service.messaging.AcknowledgementsQueueMessage.Status;
import uk.gov.hmcts.opal.service.messaging.AcknowledgementsQueuePublisher;

@Component
@Primary
public class AcknowledgementsTestPublisher implements AcknowledgementsQueuePublisher {

    Queue<AcknowledgementsQueueMessage> queuedMessages = new ArrayDeque<>();

    public void clearMessages() {
        queuedMessages.clear();
    }

    public AcknowledgementsQueueMessage getNextMessage() {
        return queuedMessages.poll();
    }

    @Override
    public void publish(long vpfpoRegistrationId, long defendantId) {
        queuedMessages.add(new AcknowledgementsQueueMessage(vpfpoRegistrationId, defendantId, Status.PUBLISHED));
    }

    @Override
    public void publishError(long vpfpoRegistrationId) {
        queuedMessages.add(new AcknowledgementsQueueMessage(vpfpoRegistrationId, null, Status.PUBLISHING_FAILED));
    }
}
