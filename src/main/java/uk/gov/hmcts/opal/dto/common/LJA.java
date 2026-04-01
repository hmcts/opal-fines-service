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
public class LJA implements ToJsonString {

    @JsonProperty("lja_id")
    private Integer ljaId;

    @JsonProperty("lja_code")
    private String ljaCode;

    @JsonProperty("lja_name")
    private String ljaName;
}
