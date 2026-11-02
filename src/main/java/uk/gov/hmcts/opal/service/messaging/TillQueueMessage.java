package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TillQueueMessage(@JsonProperty("till_id") Long tillId) {
}
