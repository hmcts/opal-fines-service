package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EnforcerReferenceData {

    @JsonProperty("enforcer_id")
    Long enforcerId;
    @JsonProperty("enforcer_code")
    Short enforcerCode;
    @JsonProperty("name")
    String name;
    @JsonProperty("name_cy")
    String nameCy;
}
