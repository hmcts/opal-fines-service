package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationDetails {

    @NotBlank
    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("organisation_aliases")
    private List<OrganisationAlias> organisationAliases;
}
