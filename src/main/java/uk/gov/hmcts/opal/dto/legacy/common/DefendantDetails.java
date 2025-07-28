package uk.gov.hmcts.opal.dto.legacy.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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

    @JsonProperty("debtor_type")
    private String debtorType;

    @JsonProperty("is_debtor")
    private Boolean isDebtor;

    @JsonProperty("organisation_flag")
    private Boolean organisationFlag;

    @JsonProperty("address")
    private AddressDetails address;

    @JsonProperty("language_preferences")
    private LanguagePreferences languagePreferences;

    @JsonProperty("organisation_details")
    private OrganisationDetails organisationDetails;

    @JsonProperty("individual_details")
    private IndividualDetails individualDetails;

    @JsonProperty("is_youth_flag")
    private Boolean isYouthFlag;

}
