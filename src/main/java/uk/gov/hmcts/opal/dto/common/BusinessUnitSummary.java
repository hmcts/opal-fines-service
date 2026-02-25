package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessUnitSummary {

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("welsh_speaking")
    private String welshSpeaking;
}
