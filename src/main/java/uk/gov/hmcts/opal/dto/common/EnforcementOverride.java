package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcementOverride implements ToJsonString {

    @JsonProperty("enforcement_override_result")
    private EnforcementOverrideResult enforcementOverrideResult;

    @JsonProperty("enforcer")
    private Enforcer enforcer;

    @JsonProperty("lja")
    private LJA lja;
}
