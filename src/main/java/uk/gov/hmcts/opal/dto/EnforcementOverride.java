package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the "enforcement_overrides" object defined in enforcementOverride.json schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnforcementOverride implements ToJsonString {

    @JsonProperty("enforcement_override_result")
    private EnforcementOverrideResultReference enforcementOverrideResult;

    @JsonProperty("enforcer")
    private EnforcerReference enforcer;

    @JsonProperty("lja")
    private LjaReference lja;
}
