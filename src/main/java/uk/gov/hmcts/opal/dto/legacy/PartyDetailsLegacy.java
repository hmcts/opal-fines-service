package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PartyDetailsLegacy {

    @JsonProperty("party_id")
    @XmlElement(name = "party_id")
    private String partyId;

    @JsonProperty("organisation_flag")
    @XmlElement(name = "organisation_flag")
    private Boolean organisationFlag;

    @JsonProperty("organisation_details")
    @XmlElement(name = "organisation_details")
    private OrganisationDetailsLegacy organisationDetails;

    @JsonProperty("individual_details")
    @XmlElement(name = "individual_details")
    private IndividualDetailsLegacy individualDetails;
}
