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
public class EnforcementOverrideResult  implements ToJsonString {

    @JsonProperty("enforcement_override_result_id")
    private String enforcementOverrideId;

    @JsonProperty("enforcement_override_result_name")
    private String enforcementOverrideTitle;
}
