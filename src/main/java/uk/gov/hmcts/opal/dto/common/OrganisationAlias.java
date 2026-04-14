package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationAlias {

    @JsonProperty("alias_id")
    private String aliasId;

    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    @JsonProperty("organisation_name")
    private String organisationName;
}
