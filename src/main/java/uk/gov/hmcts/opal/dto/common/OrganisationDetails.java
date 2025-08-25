package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationDetails {

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("organisation_aliases")
    private List<OrganisationAlias> organisationAliases;
}
