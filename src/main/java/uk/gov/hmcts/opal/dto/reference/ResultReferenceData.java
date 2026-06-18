package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultReferenceData {

    @JsonProperty("result_id")
    String resultId;
    @JsonProperty("result_title")
    String resultTitle;
    @JsonProperty("result_title_cy")
    String resultTitleCy;
    @JsonProperty("active")
    boolean active;
    @JsonProperty("result_type")
    String resultType;
    @JsonProperty("imposition_creditor")
    String impositionCreditor;
    @JsonProperty("imposition_allocation_order")
    Short impositionAllocationPriority;
}
