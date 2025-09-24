package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after updating a defendant account.
 * Uses Common Objects shape (nested groups).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountResponse implements ToJsonString {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("comment_and_notes")
    private CommentAndNotesDto commentAndNotes;

    @JsonProperty("enforcement_court")
    private CourtReferenceDto enforcementCourt;

    @JsonProperty("collection_order")
    private CollectionOrderDto collectionOrder;

    @JsonProperty("enforcement_overrides")
    private EnforcementOverride enforcementOverrides;

}
