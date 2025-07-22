package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.DateDto;

import java.time.LocalDate;

@Builder
@Data
public class DefendantDto {
    private String surname;
    private String forenames;

    @JsonProperty("exact_match_surname")
    private boolean exactMatchSurname;

    @JsonProperty("exact_match_forenames")
    private boolean exactMatchForenames;

    private String initials;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("address_line_1")
    private String addressLine1;

    private String postcode;

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    private boolean organisation;

    @JsonProperty("include_aliases")
    private boolean includeAliases;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("exact_match_organisation_name")
    private Boolean exactMatchOrganisationName;

    @JsonProperty("parent_guardian_surname")
    private String parentGuardianSurname;

    @JsonProperty("parent_guardian_firstnames")
    private String parentGuardianFirstnames;
}
