package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PartyDetailsLegacy {

    @XmlElement(name = "party_id")
    private String partyId;

    @XmlElement(name = "organisation_flag")
    private Boolean organisationFlag;

    @XmlElement(name = "organisation_details")
    private OrganisationDetailsLegacy organisationDetails;

    @XmlElement(name = "individual_details")
    private IndividualDetailsLegacy individualDetails;
}
