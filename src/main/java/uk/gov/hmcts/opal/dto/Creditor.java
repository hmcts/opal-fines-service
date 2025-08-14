package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Creditor {

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("exact_match_organisation_name")
    private Boolean exactMatchOrganisationName;

    @JsonProperty("forenames")
    private String forenames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("exact_match_surname")
    private Boolean exactMatchSurname;

    @JsonProperty("exact_match_forenames")
    private Boolean exactMatchForenames;

    @JsonProperty("organisation")
    private Boolean organisation;
}
