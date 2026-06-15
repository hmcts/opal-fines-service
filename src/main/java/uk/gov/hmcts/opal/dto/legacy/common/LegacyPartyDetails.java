package uk.gov.hmcts.opal.dto.legacy.common;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@XmlRootElement(name = "party_details")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyPartyDetails {

    @XmlElement(name = "party_id")
    private String partyId;

    @XmlElement(name = "organisation_flag")
    private Boolean organisationFlag;

    @XmlElement(name = "organisation_details")
    private OrganisationDetails organisationDetails;

    @XmlElement(name = "individual_details")
    private IndividualDetails individualDetails;
}
