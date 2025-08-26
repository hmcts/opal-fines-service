package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
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
