package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the "enforcement_override_result" object defined in enforcementOverrideResultReference.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnforcementOverrideResultReference implements ToJsonString {

    @JsonProperty("enforcement_override_result_id")
    private String enforcementOverrideResultId;

    @JsonProperty("enforcement_override_result_title")
    private String enforcementOverrideResultTitle;
}
