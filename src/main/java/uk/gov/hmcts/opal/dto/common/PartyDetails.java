package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    @JsonProperty("party_id")
    private String partyId;

    @JsonProperty("organisation_flag")
    private Boolean organisationFlag;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("organisation_details")
    private OrganisationDetails organisationDetails;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("individual_details")
    private IndividualDetails individualDetails;
}
