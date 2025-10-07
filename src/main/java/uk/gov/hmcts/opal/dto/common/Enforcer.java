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
public class Enforcer implements ToJsonString {

    @JsonProperty("enforcer_id")
    private Long enforcerId;

    @JsonProperty("enforcer_name")
    private String enforcerName;
}
