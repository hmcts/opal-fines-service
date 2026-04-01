package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountParty implements ToJsonString {

    @JsonProperty("defendant_account_party_type")
    private String defendantAccountPartyType;

    @JsonProperty("is_debtor")
    private Boolean isDebtor;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;

    @JsonProperty("address")
    private AddressDetails address;

    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    @JsonProperty("vehicle_details")
    private VehicleDetails vehicleDetails;

    @JsonProperty("employer_details")
    private EmployerDetails employerDetails;

    @JsonProperty("language_preferences")
    private LanguagePreferences languagePreferences;

}
