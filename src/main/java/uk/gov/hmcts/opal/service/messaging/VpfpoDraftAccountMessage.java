package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSnapshots.Snapshot;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSource;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VpfpoDraftAccountMessage(
    @JsonProperty("draft_account_id")
    Long draftAccountId,

    @JsonProperty("created_at")
    OffsetDateTime createdDate,

    @JsonProperty("validated_at")
    OffsetDateTime validatedDate,

    // The producer supplies the PSA/LJA code in business_unit_id.
    @JsonProperty("business_unit_id")
    Short ljaCode,

    @JsonProperty("account")
    ObjectNode account,

    @JsonProperty("account_snapshot")
    Snapshot accountSnapshot,

    @JsonProperty("account_type")
    DraftAccountType accountType,

    @JsonProperty("account_status")
    DraftAccountStatus accountStatus,

    @JsonProperty("status_message")
    String statusMessage,

    @JsonProperty("timeline_data")
    Object timelineData,

    @JsonProperty("source")
    DraftAccountSource source,

    @JsonProperty("source_reference")
    Long sourceReference
) {
}
