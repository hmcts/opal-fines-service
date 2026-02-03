package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantDetails {

    @XmlElement(name = "debtor_type")
    private String debtorType;

    @XmlElement(name = "is_debtor")
    private Boolean isDebtor;

    @XmlElement(name = "organisation_flag")
    private Boolean organisationFlag;

    @XmlElement(name = "address")
    private LegacyAddressDetails address;

    @XmlElement(name = "language_preferences")
    private LanguagePreferences languagePreferences;

    @XmlElement(name = "organisation_details")
    private LegacyOrganisationDetails organisationDetails;

    @XmlElement(name = "individual_details")
    private LegacyIndividualDetails individualDetails;

    @XmlElement(name = "is_youth_flag")
    private Boolean isYouthFlag;
}
