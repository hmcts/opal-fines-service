package uk.gov.hmcts.opal.dto.legacy.common;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.PartyDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "party_details")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPartyDetails {

    @XmlElement(name = "party_id")
    private String partyId;

    @XmlElement(name = "organisation_flag")
    private Boolean organisationFlag;

    @XmlElement(name = "organisation_details")
    private LegacyOrganisationDetails organisationDetails;

    @XmlElement(name = "individual_details")
    private LegacyIndividualDetails individualDetails;

    public  PartyDetails toOpalDto() {
        return PartyDetails.builder()
            .partyId(this.getPartyId())
            .organisationFlag(this.getOrganisationFlag())
            .organisationDetails(this.getOrganisationDetails() == null ? null : this.getOrganisationDetails().toOpalDto())
            .individualDetails(this.getIndividualDetails() == null ? null : this.getIndividualDetails().toOpalDto())
            .build();
    }
}
