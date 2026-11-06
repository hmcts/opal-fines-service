package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublishDraftAccountQueueMessage(@JsonProperty("draft_account_id") Long draftAccountId) {
}
