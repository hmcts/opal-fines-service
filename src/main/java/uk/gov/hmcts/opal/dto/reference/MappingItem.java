package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappingItem {

    @JsonProperty("code")
    private String code;

    @JsonProperty("display_name")
    private String displayName;
}
