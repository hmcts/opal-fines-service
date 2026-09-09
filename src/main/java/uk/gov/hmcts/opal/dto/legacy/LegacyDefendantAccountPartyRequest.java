package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegacyDefendantAccountPartyRequest {

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

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

    public static LegacyDefendantAccountPartyRequest from(
        Long defendantAccountPartyId, DefendantAccountParty defendantAccountParty) {
        if (defendantAccountParty == null) {
            return null;
        }

        return LegacyDefendantAccountPartyRequest.builder()
            .defendantAccountPartyId(String.valueOf(defendantAccountPartyId))
            .defendantAccountPartyType(defendantAccountParty.getDefendantAccountPartyType())
            .isDebtor(defendantAccountParty.getIsDebtor())
            .partyDetails(defendantAccountParty.getPartyDetails())
            .address(defendantAccountParty.getAddress())
            .contactDetails(defendantAccountParty.getContactDetails())
            .vehicleDetails(defendantAccountParty.getVehicleDetails())
            .employerDetails(defendantAccountParty.getEmployerDetails())
            .languagePreferences(defendantAccountParty.getLanguagePreferences())
            .build();
    }
}
