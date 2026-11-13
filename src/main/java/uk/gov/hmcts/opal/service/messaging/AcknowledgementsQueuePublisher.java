package uk.gov.hmcts.opal.service.messaging;

public interface AcknowledgementsQueuePublisher {

    void publish(long vpfpoRegistrationId, long defendantId);

    void publishError(long vpfpoRegistrationId);
}
