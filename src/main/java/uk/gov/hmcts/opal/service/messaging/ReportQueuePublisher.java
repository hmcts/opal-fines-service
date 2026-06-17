package uk.gov.hmcts.opal.service.messaging;

public interface ReportQueuePublisher {
    void publish(Long reportInstanceId);
}
