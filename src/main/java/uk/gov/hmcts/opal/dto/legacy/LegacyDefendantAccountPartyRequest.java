package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegacyDefendantAccountPartyRequest {

    @JsonProperty("defendant_account_party_id")
    private String defendantAccountPartyId;

    @JsonProperty("defendant_account_party_type")
    private DefendantAccountParty.DefendantAccountPartyTypeEnum defendantAccountPartyType;

    @JsonProperty("is_debtor")
    private Boolean isDebtor;

    @JsonProperty("party_details")
    private PartyDetailsCommonStrict partyDetails;

    @JsonProperty("address")
    private AddressDetailsCommonStrict address;

    @JsonProperty("contact_details")
    private JsonNullable<PartyContactDetailsDefendantAccount> contactDetails;

    @JsonProperty("vehicle_details")
    private JsonNullable<PartyVehicleDetailsDefendantAccount> vehicleDetails;

    @JsonProperty("employer_details")
    private JsonNullable<PartyEmployerDetailsDefendantAccount> employerDetails;

    @JsonProperty("language_preferences")
    private JsonNullable<LanguagePreferencesCommonStrict> languagePreferences;

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
