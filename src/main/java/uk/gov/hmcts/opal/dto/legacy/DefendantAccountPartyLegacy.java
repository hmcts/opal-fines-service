package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantAccountPartyLegacy {

    @XmlElement(name = "defendant_account_party_type")
    private String defendantAccountPartyType;

    @XmlElement(name = "is_debtor")
    private Boolean isDebtor;

    @XmlElement(name = "party_details")
    private PartyDetailsLegacy partyDetails;

    @XmlElement(name = "address")
    private AddressDetailsLegacy address;

    @XmlElement(name = "contact_details")
    private ContactDetailsLegacy contactDetails;

    @XmlElement(name = "vehicle_details")
    private VehicleDetailsLegacy vehicleDetails;

    @XmlElement(name = "employer_details")
    private EmployerDetailsLegacy employerDetails;

    @XmlElement(name = "language_preferences")
    private LanguagePreferencesLegacy languagePreferences;
}
