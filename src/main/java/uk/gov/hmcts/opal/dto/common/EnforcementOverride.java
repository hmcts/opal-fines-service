package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcerReferenceCommon;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnforcementOverride implements ToJsonString {

    @JsonProperty("enforcement_override_result")
    private EnforcementOverrideResultReferenceCommon enforcementOverrideResult;

    @JsonProperty("enforcer")
    private EnforcerReferenceCommon enforcer;

    @JsonProperty("lja")
    private LJA lja;
}
