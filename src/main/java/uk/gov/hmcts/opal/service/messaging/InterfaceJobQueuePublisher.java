package uk.gov.hmcts.opal.service.messaging;

import java.util.List;

public interface InterfaceJobQueuePublisher {

    void publish(List<Long> interfaceJobIds);
}
