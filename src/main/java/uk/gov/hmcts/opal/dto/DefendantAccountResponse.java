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
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.util.Versioned;

/**
 * Response returned after updating a defendant account.
 * Uses Common Objects shape (nested groups).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DefendantAccountResponse implements ToJsonString, Versioned {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("comment_and_notes")
    private CommentsAndNotes commentsAndNotes;

    @JsonProperty("enforcement_court")
    private CourtReferenceDto enforcementCourt;

    @JsonProperty("collection_order")
    private CollectionOrderDto collectionOrder;

    @JsonProperty("enforcement_override")
    private EnforcementOverride enforcementOverride;

    @JsonIgnore
    private BigInteger version;

}
