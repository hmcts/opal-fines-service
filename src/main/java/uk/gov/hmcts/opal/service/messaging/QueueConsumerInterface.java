package uk.gov.hmcts.opal.service.messaging;

public interface QueueConsumerInterface {

    /**
     * Handles queue messages once they are received from Azure Service Bus.
     */

    void consume(String messagePayload);

}
