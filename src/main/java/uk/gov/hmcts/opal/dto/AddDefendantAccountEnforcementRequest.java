package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddDefendantAccountEnforcementRequest {

    @JsonProperty("result_id")
    private ResultId resultId;

    @JsonProperty("enforcement_result_responses")
    private List<ResultResponse> enforcementResultResponses;

    @JsonProperty("payment_terms")
    private PaymentTerms paymentTerms; // assume this class already exists in your project
}
