package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultReferenceData(
        @JsonProperty("result_id")  String resultId,
        @JsonProperty("result_title") String resultTitle,
        @JsonProperty("result_title_cy") String resultTitleCy,
        @JsonProperty("active") boolean active,
        @JsonProperty("result_type") String resultType,
        @JsonProperty("imposition_creditor") String impositionCreditor,
        @JsonProperty("imposition_allocation_order") Short impositionAllocationPriority) {
}
