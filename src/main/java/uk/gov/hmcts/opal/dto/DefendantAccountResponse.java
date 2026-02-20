package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.util.Versioned;

/**
 * Response returned after updating a defendant account.
 * Uses endpoint-specific groups to match the update schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountResponse implements ToJsonString, Versioned {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("comment_and_notes")
    private CommentsAndNotes commentsAndNotes;

    @JsonProperty("enforcement_court")
    private EnforcementCourtResponse enforcementCourt;

    @JsonProperty("collection_order")
    private CollectionOrderResponse collectionOrder;

    @JsonProperty("enforcement_override")
    private EnforcementOverrideResponse enforcementOverride;

    @JsonIgnore
    private BigInteger version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EnforcementCourtResponse implements ToJsonString {

        @JsonProperty("enforcing_court_id")
        private Long enforcingCourtId;

        @JsonProperty("court_name")
        private String courtName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CollectionOrderResponse implements ToJsonString {

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
    public static class EnforcementOverrideResponse implements ToJsonString {

        @JsonProperty("enf_override_result_id")
        private String enforcementOverrideResultId;

        @JsonProperty("enf_override_enforcer_id")
        private Long enforcementOverrideEnforcerId;

        @JsonProperty("enf_override_tfo_lja_id")
        private Integer enforcementOverrideTfoLjaId;
    }
}
