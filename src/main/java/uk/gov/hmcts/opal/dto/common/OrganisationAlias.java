package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationAlias {

    @JsonProperty("alias_id")
    private String aliasId;

    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    @JsonProperty("organisation_name")
    private String organisationName;
}
