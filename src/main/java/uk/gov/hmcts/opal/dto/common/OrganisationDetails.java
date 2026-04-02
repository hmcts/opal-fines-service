package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationDetails {

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("organisation_aliases")
    private List<OrganisationAlias> organisationAliases;
}
