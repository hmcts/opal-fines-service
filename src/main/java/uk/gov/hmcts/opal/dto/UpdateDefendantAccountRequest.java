package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;

/**
 * Request payload for PATCH /defendant-accounts/{id} (Opal mode).
 * Exactly one update group must be present. No IDs in the body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDefendantAccountRequest implements ToJsonString {

    @JsonProperty("comment_and_notes")
    private CommentsAndNotes commentsAndNotes;

    @JsonProperty("enforcement_court")
    private EnforcementCourtRequest enforcementCourt;

    @JsonProperty("collection_order")
    private CollectionOrderRequest collectionOrder;

    @JsonProperty("enforcement_override")
    private EnforcementOverrideRequest enforcementOverride;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnforcementCourtRequest implements ToJsonString {

        @JsonProperty("enforcing_court_id")
        private Integer enforcingCourtId;

        @JsonProperty("court_name")
        private String courtName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CollectionOrderRequest implements ToJsonString {

        @JsonProperty("collection_order")
        private Boolean collectionOrder;

        @JsonProperty("collection_order_date")
        private String collectionOrderDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnforcementOverrideRequest implements ToJsonString {

        @JsonProperty("enf_override_result_id")
        private String enforcementOverrideResultId;

        @JsonProperty("enf_override_enforcer_id")
        private Integer enforcementOverrideEnforcerId;

        @JsonProperty("enf_override_tfo_lja_id")
        private Integer enforcementOverrideTfoLjaId;
    }
}
