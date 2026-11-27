package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public record AcknowledgementsQueueMessage(
    @JsonProperty("vpfpo_registration_id") Long registrationId,
    @JsonProperty("defendant_id") Long defendant_id,
    @JsonProperty("status") Status status) {

    enum Status {
        PUBLISHED, PUBLISHING_FAILED
    }
}
