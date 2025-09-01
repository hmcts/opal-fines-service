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

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

    @JsonProperty("organisation_flag")
    private Boolean organisationFlag;

    @JsonProperty("organisation_details")
    private OrganisationDetails organisationDetails;

    @JsonProperty("individual_details")
    private IndividualDetails individualDetails;
}
