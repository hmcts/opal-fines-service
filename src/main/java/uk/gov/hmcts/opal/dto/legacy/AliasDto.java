package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AliasDto {

    @JsonProperty("alias_number")
    private Integer aliasNumber;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("forenames")
    private String forenames;
}
