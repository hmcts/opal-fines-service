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
public class DefendantAccountParty {

    @JsonProperty("defendant_account_party_type")
    private String defendantAccountPartyType;

    @JsonProperty("is_debtor")
    private Boolean isDebtor;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;

    @JsonProperty("address")
    private AddressDetails address;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("vehicle_details")
    private VehicleDetails vehicleDetails;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("employer_details")
    private EmployerDetails employerDetails;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("language_preferences")
    private LanguagePreferences languagePreferences;

}
