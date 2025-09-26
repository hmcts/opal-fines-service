package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LastEnforcementAction implements ToJsonString {

    @JsonProperty("last_enforcement_action_id")
    private String lastEnforcementActionId;

    @JsonProperty("last_enforcement_action_title")
    private String lastEnforcementActionTitle;
}
