package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TillQueueMessage(
    @JsonProperty("till_id") Long tillId,
    @JsonProperty("business_unit_id") Short businessUnitId,
    @JsonProperty("posted_by") String postedBy,
    @JsonProperty("posted_by_name") String postedByName,
    @JsonProperty("requested_by") Long requestedBy
) {
}
