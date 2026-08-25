package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InterfaceJobQueueMessage(
    @JsonProperty("interface_job_id")
    Long interfaceJobId
) {
}
