package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the "enforcer" object defined in enforcerReference.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnforcerReference implements ToJsonString {

    @JsonProperty("enforcer_id")
    private Integer enforcerId;

    @JsonProperty("enforcer_name")
    private String enforcerName;
}
