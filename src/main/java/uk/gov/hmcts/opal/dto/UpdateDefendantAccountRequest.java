package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for PATCH /defendant-accounts/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDefendantAccountRequest implements ToJsonString {

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_user_id")
    private String businessUnitUserId;

    @JsonProperty("comment_and_notes")
    private CommentAndNotesDto commentAndNotes;

    @JsonProperty("enforcement_court")
    private CourtReferenceDto enforcementCourt;

    @JsonProperty("collection_order")
    private CollectionOrderDto collectionOrder;

    @JsonProperty("enforcement_overrides")
    private EnforcementOverride enforcementOverrides;
}
